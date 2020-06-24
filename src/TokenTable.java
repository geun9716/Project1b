import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	/* 프로그램의 section별 길이를 저장하는 공간*/
	int length;
	
	/* REGISTER의 값을 저장하는 HASHMAP*/
	HashMap<String, Integer> registMap;
	
	/* 프로그램의 Modify 정보를 저장하는 공간*/
	ArrayList<Modify> M;
	
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	/**
	 * 초기화하면서 symTable, literalTable 그리고 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab){
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
		tokenList = new ArrayList<Token>();

		M = new ArrayList<Modify>();
		/*REGISTER 이름과 값 초기화*/
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
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		Token tk = new Token(line, instTab);
		tokenList.add(tk);
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
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
				else if (format == 2)																	//2형식
				{
					object |= instTab.search_opcode(getToken(index).operator) << 8;
					object |= registMap.get(getToken(index).operand[0]) << 4;
					
					if (getToken(index).operand.length > 1)							//REGISTER
						object |= registMap.get(getToken(index).operand[1]);
					
					getToken(index).objectCode = Integer.toHexString(object).toUpperCase();
				}
				else if (format == 3)																	//3형식
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
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;
	
	//parsing link
	InstTable instTab;
	
	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line, InstTable instTab) {
		//initialize 추가
		operand = new String[1];
		label = "";	operator = ""; operand[0] = "";	comment = "";
		objectCode = "";		byteSize = 0;
		this.instTab = instTab;
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
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
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
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
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	/*
	 * Token을 볼 수 있는 toString 함수.
	 * 사용예: tk.toString()
	 * */
	public String toString() {
		return new String(Integer.toHexString(location)+"\t"+label+"\t"+operator+"\t"+operand[0]);
	}
}
/* Modified CODE 생성을 위한 CLASS
 * location, length, Flag, operand의 값을 가진다.
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
