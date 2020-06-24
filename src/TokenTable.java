import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	/* ���α׷��� section�� ���̸� �����ϴ� ����*/
	int length;
	
	/* REGISTER�� ���� �����ϴ� HASHMAP*/
	HashMap<String, Integer> registMap;
	
	/* ���α׷��� Modify ������ �����ϴ� ����*/
	ArrayList<Modify> M;
	
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable, literalTable �׸��� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param literalTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab){
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
		tokenList = new ArrayList<Token>();

		M = new ArrayList<Modify>();
		/*REGISTER �̸��� �� �ʱ�ȭ*/
		registMap = new HashMap<String, Integer>();
		registMap.put("A", 0);
		registMap.put("X", 1);
		registMap.put("L", 2);
		registMap.put("B", 3);
		registMap.put("S", 4);
		registMap.put("T", 5);
		registMap.put("F", 6);
		registMap.put("PC", 8);
		registMap.put("SW", 9);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		Token tk = new Token(line, instTab);
		tokenList.add(tk);
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index
	 */
	public void makeObjectCode(int index){
		String tmp;
		int object = 0 , format = 0 , targetAddr = 0;
		if (!getToken(index).operator.isEmpty())										
		{
			if(getToken(index).operator.equals("START")
					||getToken(index).operator.equals("CSECT"))						//H RECORD
			{
				getToken(index).objectCode = "NO";
			}
			else if(getToken(index).operator.equals("EXTDEF"))						//D RECORD
			{
				getToken(index).objectCode = "NO";
			}
			else if(getToken(index).operator.equals("EXTREF"))						//R RECORD
			{
				getToken(index).objectCode = "NO";
			}
			else if(getToken(index).operator.equals("LTORG")
					||getToken(index).operator.equals("END"))							//LTORT & END
			{
				for(int i = 0 ; i < literalTab.literalList.size(); i++)
				{
					if (literalTab.literalList.get(i).charAt(1)=='C')						//CHAR TYPE
					{
						tmp = literalTab.literalList.get(i).substring(3, 
								literalTab.literalList.get(i).length()-1);
						for (int j = 0 ; j < tmp.length(); j++)
						{
							object |= (int)tmp.charAt(j) << (tmp.length() - j - 1) * 8;
						}
						getToken(index).objectCode = String.format("%06X", object);
					}
					else if (literalTab.literalList.get(i).charAt(1)=='X')					//HEX TYPE
					{
						tmp = literalTab.literalList.get(i).substring(3, 
								literalTab.literalList.get(i).length()-1);
						object |= Integer.parseInt(tmp,16);
						getToken(index).objectCode = String.format("%02X", object);
					} /*
						 * else { tmp = literalTab.literalList.get(i).substring(1); object |=
						 * Integer.parseInt(tmp); getToken(index++).objectCode = String.format("%06X",
						 * object); }
						 */
				}
			}
			else if (getToken(index).operator.equals("WORD"))							//WORD
			{
				if(getToken(index).operand[0].charAt(0) >= '0' 
						&& getToken(index).operand[0].charAt(0) <= '9')
				{
					object |= Integer.parseInt(getToken(index).operand[0]);
				}
				else
				{
					getToken(index).objectCode = String.format("%06X", 0);
					if(getToken(index).operand[0].contains("-"))						//MODIFY
					{
						String arr[] = getToken(index).operand[0].split("-");
						
						M.add(new Modify(getToken(index).location, 6, '+',arr[0]));
						M.add(new Modify(getToken(index).location, 6, '-',arr[1]));
					}
				}
			}
			else if (getToken(index).operator.equals("BYTE"))							//BYTE
			{
				if (getToken(index).operand[0].charAt(0) == 'X')
				{
					tmp = getToken(index).operand[0].substring(2, 
							getToken(index).operand[0].length()-1);
					object |= Integer.parseInt(tmp,16);
					getToken(index).objectCode = String.format("%02X", object);
				}
			}
			else if ((format = instTab.search_format(getToken(index).operator))>0)	//NORMAL INST
			{
				if (getToken(index).operator.charAt(0)=='+')							//EXTENDED
				{
					object |= instTab.search_opcode(getToken(index).operator) << 24;
					object |= getToken(index).nixbpe << 20;
					//ADD Modify List
					M.add(new Modify(getToken(index).location+1, 5, '+',getToken(index).operand[0]));
					
				}
				else if (format == 2)																	//2����
				{
					object |= instTab.search_opcode(getToken(index).operator) << 8;
					object |= registMap.get(getToken(index).operand[0]) << 4;
					
					if (getToken(index).operand.length > 1)							//REGISTER
						object |= registMap.get(getToken(index).operand[1]);
					
					getToken(index).objectCode = Integer.toHexString(object).toUpperCase();
				}
				else if (format == 3)																	//3����
				{
					object |= instTab.search_opcode(getToken(index).operator) << 16;
					object |= getToken(index).nixbpe << 12;
					
					if (getToken(index).getFlag(TokenTable.nFlag) > 0 
							&& getToken(index).getFlag(TokenTable.iFlag)>0)			//SIC/XE NORMAL
					{
						if(!getToken(index).operand[0].isEmpty())
							targetAddr = symTab.search(getToken(index).operand[0])
											-getToken(index+1).location;
					}
					else if (getToken(index).getFlag(TokenTable.nFlag)>0)				//INDIRECT
					{
						targetAddr = symTab.search(getToken(index).operand[0].substring(1))
											-getToken(index+1).location;
					}
					else if (getToken(index).getFlag(TokenTable.iFlag)>0)				//IMMEDIATE
					{
						targetAddr = Integer.parseInt(getToken(index).operand[0].substring(1));
					}
				}
			}
			else
			{
				getToken(index).objectCode = "NO";
			}
		}
		else
		{
			getToken(index).objectCode = "NO";
		}
		if (!getToken(index).operand[0].isEmpty()
				&& getToken(index).operand[0].charAt(0) == '=')						//LITERAL VALUE
		{
			int value = literalTab.search(getToken(index).operand[0]);
			if (value > 0)
				targetAddr = value - tokenList.get(index+1).location;
		}
		if (targetAddr < 0)															//NAGATIVE TARGET_ADDRESS
		{
			targetAddr &= 4095;
		}
		
		object |= targetAddr;														//MAKE OBJECT CODE
		
		if (getToken(index).objectCode.isEmpty())
		{
			getToken(index).objectCode = String.format("%06X", object);
		}
	}
	
	/** 
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;
	
	//parsing link
	InstTable instTab;
	
	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line, InstTable instTab) {
		//initialize �߰�
		operand = new String[1];
		label = "";	operator = ""; operand[0] = "";	comment = "";
		objectCode = "";		byteSize = 0;
		this.instTab = instTab;
		parsing(line);
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		String []arr = line.split("\t");
		String []temp;
		
		if(arr[0].isEmpty())							//haven't label
		{
			operator = arr[1];
			
			if (arr.length > 2)							//have operand
			{
				temp = arr[2].split(",");
				operand = new String[temp.length];
				for (int i = 0 ; i < temp.length ; i++)
				{
					operand[i] = temp[i];
				}
			}		
			
			if(arr.length > 3)							//have comment
				comment = arr[3];
		}
		else if (arr[0].charAt(0) == '.')				//remark Token
		{
			label = arr[0];
		}
		else											//have label
		{
			label = arr[0];
			operator = arr[1];							
			
			if (arr.length > 2)							//have operand
			{
				temp = arr[2].split(",");
				operand = new String[temp.length];
				for (int i = 0 ; i < temp.length ; i++)
				{
					operand[i] = temp[i];
				}
			}
			
			if(arr.length > 3)							//have comment
				comment = arr[3];
		}
		// Set_nixbpe
		if (!operator.isEmpty())
		{
			if (operator.charAt(0) == '+')						//EXTENDED
				setFlag(TokenTable.eFlag, 1);
			else if (instTab.search_format(operator) == 3)		
				setFlag(TokenTable.pFlag, 1);
			
			if(!operand[0].isEmpty())
			{
				if(operand[0].charAt(0) == '#')					//IMMEDIATE
				{
					setFlag(TokenTable.pFlag, 0);
					setFlag(TokenTable.iFlag, 1);
				}
				else if (operand[0].charAt(0) == '@')			//INDIRECT
				{
					setFlag(TokenTable.nFlag, 1);
				}
				else if (instTab.search_format(operator) == 3)	//NORMAL TYPE
				{
					setFlag(TokenTable.iFlag, 1);
					setFlag(TokenTable.nFlag, 1);
				}
				
				if (operand.length > 1)
					if (operand[1].charAt(0) == 'X')
						setFlag(TokenTable.xFlag, 1);
			}
			else if (operator.equals("RSUB"))					//RSUB
			{
				setFlag(TokenTable.pFlag, 0);
				setFlag(TokenTable.iFlag, 1);
				setFlag(TokenTable.nFlag, 1);
			}
		}
	}
	
	/** 
	 * n,i,x,b,p,e flag�� �����Ѵ�. 
	 * 
	 * ��� �� : setFlag(nFlag, 1); 
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value)
	{		
		double temp = Math.log(flag)/Math.log(2);
		if (value == 1)
			nixbpe |= (value << (int)temp);
		else if (value == 0)
			nixbpe &= (value << (int)temp);
	}
	
	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� 
	 * 
	 * ��� �� : getFlag(nFlag)
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	/*
	 * Token�� �� �� �ִ� toString �Լ�.
	 * ��뿹: tk.toString()
	 * */
	public String toString() {
		return new String(Integer.toHexString(location)+"\t"+label+"\t"+operator+"\t"+operand[0]);
	}
}
/* Modified CODE ������ ���� CLASS
 * location, length, Flag, operand�� ���� ������.
 * */
class Modify {
	
	int location;
	int length;
	char Flag;
	String operand;
	Modify(int loc, int len, char Flag, String operand){
		this.location = loc;
		this.length = len;
		this.Flag = Flag;
		this.operand = operand;
	}
}
