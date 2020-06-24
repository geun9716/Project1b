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
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 * 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */


public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	
	ArrayList<String> codeList;
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
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
	 * 어셈블러의 메인 루틴
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
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
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
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		
		//Parsing
		TokenTable tkt = new TokenTable(new SymbolTable(), new LiteralTable(), instTable);
		for (int i = 0 ; i < lineList.size() ; i++)
		{
			tkt.putToken(lineList.get(i));
		}
		
		//프로그램별 TokenTable을 TokenList에 저장 
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
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
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
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
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
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
		int size = 0;
		String tmp = "";
		String T = "";
		String temp = "";
		for(int n = 0 ; n < TokenList.size(); n++)												//프로그램 별 TokenTable
		{
			for(int m = 0 ; m < TokenList.get(n).tokenList.size(); m++)							//TokenTable별 TokenList
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
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
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
