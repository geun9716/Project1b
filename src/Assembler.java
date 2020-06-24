import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Assembler : 
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. 
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) 
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) 
 * 
 * 
 * �ۼ����� ���ǻ��� : 
 * 
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 *     
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */


public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� literal table�� �����ϴ� ����*/
	ArrayList<LiteralTable> literaltabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����.   
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	
	ArrayList<String> codeList;
	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * ������� ���� ��ƾ
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler(".\\src\\inst.data");
		assembler.loadInputFile(".\\src\\input.txt");	
		assembler.pass1();
		assembler.printSymbolTable(".\\src\\symtab_20160262.txt");
		assembler.printLiteralTable(".\\src\\literaltab_20160262.txt");
		
		assembler.pass2();
		assembler.printObjectCode("C:.\\src\\output_20160262.txt");
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.
	 * @param inputFile : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		try {
			File file = new File(inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			while((line = bufReader.readLine()) != null)
			{
				lineList.add(line);
			}
			bufReader.close();
		} catch (FileNotFoundException e) {
			e.getStackTrace();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/** 
	 * pass1 ������ �����Ѵ�.
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����
	 *   2) label�� symbolTable�� ����
	 *   
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		
		//Parsing
		TokenTable tkt = new TokenTable(new SymbolTable(), new LiteralTable(), instTable);
		for (int i = 0 ; i < lineList.size() ; i++)
		{
			tkt.putToken(lineList.get(i));
		}
		
		//���α׷��� TokenTable�� TokenList�� ���� 
		SymbolTable symtmp = new SymbolTable();
		LiteralTable littmp = new LiteralTable();
		TokenTable tmp = new TokenTable(symtmp, littmp, instTable);
		int locctr = 0;
		int format2;
		
		for (int i = 0 ; i < tkt.tokenList.size();i++)
		{
			tkt.getToken(i).location = locctr;
			
			if (!tkt.getToken(i).label.isEmpty() 
					&& !(tkt.getToken(i).label.charAt(0) == '.'))
			{
				if (tkt.getToken(i).operator.equals("CSECT"))								//CSECT
				{
					tmp.length = locctr;
					//SET location = 0
					locctr = 0;
					tkt.getToken(i).location = 0;
					
					//ADD Program Table
					tmp.symTab = symtmp;
					tmp.literalTab = littmp;
					
					TokenList.add(tmp);
					symtabList.add(symtmp);
					literaltabList.add(littmp);
					//NEW TABLE
					symtmp = new SymbolTable();
					littmp = new LiteralTable();
					tmp = new TokenTable(symtmp, littmp, instTable);
				}
				symtmp.putSymbol(tkt.getToken(i).label, tkt.getToken(i).location);	//PUT SYMTAB
			}
			if(!tkt.getToken(i).operator.isEmpty())
			{
				if(tkt.getToken(i).operator.equals("RESW"))		 						//RESW
				{
					locctr += 3*Integer.parseInt(tkt.getToken(i).operand[0]);
					tkt.getToken(i).byteSize = 3*Integer.parseInt(tkt.getToken(i).operand[0]);
				}
				else if(tkt.getToken(i).operator.equals("RESB"))							//RESB
				{
					tkt.getToken(i).byteSize = Integer.parseInt(tkt.getToken(i).operand[0]);
					locctr += Integer.parseInt(tkt.getToken(i).operand[0]);
				}
				else if(tkt.getToken(i).operator.equals("WORD"))							//WORD 
				{
					tkt.getToken(i).byteSize = 3;
					locctr += 3;
				}
				else if(tkt.getToken(i).operator.equals("BYTE"))							//BYTE
				{
					if(tkt.getToken(i).operand[0].equals("X"))
					{
						tkt.getToken(i).byteSize = 1;
						locctr += 1;
					}
					else
					{
						tkt.getToken(i).byteSize = 1;
						locctr += 1;
					}
				}
				else if(tkt.getToken(i).operator.equals("EQU"))							//EQU
				{
					if(tkt.getToken(i).operand[0].equals("*"))
						;
					else
					{
						if(tkt.getToken(i).operand[0].contains("-"))
						{
							String arr[] = tkt.getToken(i).operand[0].split("-");
							
							tkt.getToken(i).location = symtmp.search(arr[0]) - symtmp.search(arr[1]);
							symtmp.modifySymbol(tkt.getToken(i).label, tkt.getToken(i).location);		//CALCULATE EQU
						}
					}
				}
				else if(tkt.getToken(i).operator.equals("LTORG")
						||tkt.getToken(i).operator.equals("END"))		//LTORG & END
				{
					for(int j = 0 ; j < littmp.literalList.size(); j++)
					{
						littmp.modifyLiteral(littmp.literalList.get(j), tkt.getToken(i).location);
						
						if(littmp.literalList.get(j).charAt(1) == 'C')									//CHAR TYPE
						{
							locctr += littmp.literalList.get(j).substring(3, littmp.literalList.get(j).length()-1).length();
							tkt.getToken(i).byteSize 
							= littmp.literalList.get(j).substring(3, littmp.literalList.get(j).length()-1).length();
							//tkt.getToken(i).location = locctr;
						}
						else if(littmp.literalList.get(j).charAt(1) == 'X')								//HEX TYPE
						{
							int len = littmp.literalList.get(j).substring(3, littmp.literalList.get(j).length()-1).length();
							if((len % 2) > 0)
							{
								locctr += len/2 +1;
								tkt.getToken(i).byteSize = len/2 +1;
							}
							else
							{
								locctr += len/2;
								tkt.getToken(i).byteSize = len/2;
							}
						}
						else 
						{
							littmp.modifyLiteral(littmp.literalList.get(j), locctr);
							locctr += 3;
							tkt.getToken(i).byteSize = 3;
						}
							
					}
				}
				else if((format2 = instTable.search_format(tkt.getToken(i).operator)) > 0)			//NORMAL INST
				{
					if(tkt.getToken(i).operator.charAt(0)=='+')										//EXTENDED
					{
						locctr += 4;
						tkt.getToken(i).byteSize = 4;
					}
					else																			//OTHER INST
					{
						locctr += format2;
						tkt.getToken(i).byteSize = format2;
					}
				}
			}
			if(!tkt.getToken(i).operand[0].isEmpty()&& tkt.getToken(i).operand[0].charAt(0) == '=')	//PUT LITERAL TABLE
			{
				int index = littmp.search(tkt.getToken(i).operand[0]);
				if (index < 0)
					littmp.putLiteral(tkt.getToken(i).operand[0], tkt.getToken(i).location);
			}
			tmp.tokenList.add(tkt.getToken(i));														//ADD_Token_in_tmp
		}
		
		//Last Tables ADD
		tmp.length = locctr;

		tmp.symTab = symtmp;
		tmp.literalTab = littmp;
		
		TokenList.add(tmp);
		symtabList.add(symtmp);
		literaltabList.add(littmp);
	}
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		try {
			File file = new File(fileName);
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(file));
			if (file.isFile() && file.canWrite())
			{
				for (int i = 0 ; i < symtabList.size(); i++)
				{
					for (int j = 0 ; j <symtabList.get(i).symbolList.size(); j++)
					{
						bufWriter.write(symtabList.get(i).symbolList.get(j)+"\t"
								+Integer.toHexString(symtabList.get(i).locationList.get(j))+"\n");
						System.out.println(symtabList.get(i).symbolList.get(j)+"\t"
								+Integer.toHexString(symtabList.get(i).locationList.get(j)));
					}
					bufWriter.newLine();
					System.out.println();
				}
				bufWriter.close();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		String temp;
		try {
			File file = new File(fileName);
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(file));
			if (file.isFile() && file.canWrite())
			{
				for (int i = 0 ; i < literaltabList.size(); i++)
				{
					for (int j = 0 ; j <literaltabList.get(i).literalList.size(); j++)
					{
						if (literaltabList.get(i).literalList.get(j).charAt(1) == 'C' || literaltabList.get(i).literalList.get(j).charAt(1) == 'X')
							temp = literaltabList.get(i).literalList.get(j).substring(3, literaltabList.get(i).literalList.get(j).length()-1);
						else
							temp = literaltabList.get(i).literalList.get(j).substring(1);
						bufWriter.write(temp+"\t"+Integer.toHexString(literaltabList.get(i).locationList.get(j))+"\n");
						System.out.println(temp);
					}
				}
				bufWriter.close();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * pass2 ������ �����Ѵ�.
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		System.out.println("�ѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤ�");
		int size = 0;
		String tmp = "";
		String T = "";
		String temp = "";
		for(int n = 0 ; n < TokenList.size(); n++)												//���α׷� �� TokenTable
		{
			for(int m = 0 ; m < TokenList.get(n).tokenList.size(); m++)							//TokenTable�� TokenList
			{
				TokenList.get(n).makeObjectCode(m);												//Make OJBECT CODE
				
				if (!TokenList.get(n).getToken(m).operator.isEmpty())										
				{
					if(TokenList.get(n).getToken(m).operator.equals("START")
							||TokenList.get(n).getToken(m).operator.equals("CSECT"))			//H RECORD
					{
						tmp = "H"+symtabList.get(n).symbolList.get(0)+'\t'
								+String.format("%06d",symtabList.get(n).locationList.get(0))
								+String.format("%06X",TokenList.get(n).length);
						codeList.add(tmp);
					}
					else if(TokenList.get(n).getToken(m).operator.equals("EXTDEF"))				//D RECORD
					{
						tmp = "D";
						for(int i = 0 ; i < TokenList.get(n).getToken(m).operand.length; i++)
						{
							if(!TokenList.get(n).getToken(m).operand[i].isEmpty())
							{
								tmp += TokenList.get(n).getToken(m).operand[i]
								+String.format("%06X", symtabList.get(n).search(TokenList.get(n).getToken(m).operand[i]));
							}
						}
						codeList.add(tmp);
					}
					else if(TokenList.get(n).getToken(m).operator.equals("EXTREF"))				//R RECORD
					{
						tmp = "R";
						for(int i = 0 ; i < TokenList.get(n).getToken(m).operand.length; i++)
						{
							if(!TokenList.get(n).getToken(m).operand[i].isEmpty())
							{
								tmp += TokenList.get(n).getToken(m).operand[i];
							}
						}
						codeList.add(tmp);
					}
				}
				
				//T RECORD
				if (m == 0)
				{
					T = "T"+String.format("%06X", TokenList.get(n).getToken(m).location);
				}
					
				if (!TokenList.get(n).getToken(m).objectCode.equals("NO"))
				{
					if((size + TokenList.get(n).getToken(m).byteSize) > 0x1e)					//T LIMIT 0x1E
					{
						T += String.format("%02X", temp.length()/2)+temp;
						codeList.add(T);
						T = "T"+String.format("%06X", TokenList.get(n).getToken(m).location);
						size = 0;
						temp = "";
					}
					if(TokenList.get(n).tokenList.get(m-1).objectCode.equals("NO"))
					{
						T = "T"+String.format("%06X", TokenList.get(n).getToken(m).location);
					}
					if(TokenList.get(n).tokenList.get(m).operator.equals("LTORG")) { 
						int object = 0;
						for(int i = 0 ; i < TokenList.get(n).literalTab.literalList.size(); i++, object = 0) 
						{
							if (TokenList.get(n).literalTab.literalList.get(i).charAt(1)=='C')						//CHAR TYPE
							{
								tmp = TokenList.get(n).literalTab.literalList.get(i).substring(3, 
										TokenList.get(n).literalTab.literalList.get(i).length()-1);
								for (int j = 0 ; j < tmp.length(); j++)
								{
									object |= (int)tmp.charAt(j) << (tmp.length() - j - 1) * 8;
								}
								temp += String.format("%06X", object);
								size += 3;
							}
							else 
							{
								tmp = TokenList.get(n).literalTab.literalList.get(i).substring(1);
								object |= Integer.parseInt(tmp);
								temp += String.format("%06X",object);
								size += 3;
							}
						}
					}
					else
					{
						temp += TokenList.get(n).getObjectCode(m);
						size += TokenList.get(n).getToken(m).byteSize;
					}
				}
				else
				{
					if(!TokenList.get(n).getToken(m).operator.isEmpty()
							&&TokenList.get(n).getToken(m).operator.equals("CSECT"))			//CSECT
					{
						T = "T"+String.format("%06X", TokenList.get(n).getToken(m).location);
						size = 0;
						temp = "";
					}
					else if (m > 1 && !TokenList.get(n).getToken(m-1).objectCode.equals("NO"))
					{
						T += String.format("%02X", temp.length()/2)+temp;
						codeList.add(T);
						size = 0;
						temp = "";
					}
				}
				
				System.out.println(TokenList.get(n).getToken(m).toString()+"\t"+TokenList.get(n).getObjectCode(m));
				
			}
			if (!temp.isEmpty())																//Last Buffer ADD
			{
				T += String.format("%02X", size)+temp;
				codeList.add(T);
				size = 0;
				temp = "";
			}
			
			//M RECORD
			for(int i = 0 ; i < TokenList.get(n).M.size(); i++)
			{
				codeList.add("M"+String.format("%06X", TokenList.get(n).M.get(i).location) 
									+String.format("%02X", TokenList.get(n).M.get(i).length)
									+TokenList.get(n).M.get(i).Flag +TokenList.get(n).M.get(i).operand);
			}
			
			//E RECORD
			if (n == 0)
				codeList.add("E"+String.format("%06X",symtabList.get(n).locationList.get(0)));
			else
				codeList.add("E");
			
			codeList.add(" ");
		}
	}
	
	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		System.out.println("�ѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤѤ�");
		try {
			File file = new File(fileName);
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(file));
			if (file.isFile() && file.canWrite())
			{
				for (int i = 0 ; i < codeList.size(); i++)
				{
					bufWriter.write(codeList.get(i)+'\n');
					System.out.println(codeList.get(i));
				}
				bufWriter.close();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
