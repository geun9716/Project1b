import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.String;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	public void openFile(String fileName) {
		try {
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader buffReader = new BufferedReader(filereader);
			String line = "";
			while((line = buffReader.readLine()) != null)
			{
				String [] inst = line.split(" ", 2);
				Instruction temp = new Instruction(line);
				instMap.put(inst[0], temp);
			}
			buffReader.close();
		} catch (FileNotFoundException e) {
			e.getStackTrace();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	/*
	 * �Է¹��� str���� ���� Inst�� ã�Ƽ� format�� return
	 * ���� : (int)format , ���� : -1*/
	public int search_format(String str)
	{
		if (str == null)
			return -1;
		if (str.charAt(0) == '+')
			str = str.substring(1);
		if (instMap.containsKey(str))
			return instMap.get(str).format;
		else
			return -1;
	}
	/*
	 * �Է¹��� str���� ���� Inst�� ã�Ƽ� opcode�� return
	 * ���� : (int)opcode , ���� : -1*/
	public int search_opcode(String str)
	{
		if (str == null)
			return -1;
		
		if (str.charAt(0)=='+')
			str = str.substring(1);
		
		if (instMap.containsKey(str))
			return instMap.get(str).opcode;
		else
			return -1;
		
	}
	
	//get, set, search ���� �Լ��� ���� ����

}
/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	/* 
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 *  
	 * ex)
	 * String instruction;
	 * int opcode;
	 * int numberOfOperand;
	 * String comment;
	 */
	
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	
	String instruction;
	int format;
	int opcode;
	int numberOfOperand;
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		String[] info = line.split(" ");
		instruction = info[0];
		format = Integer.parseInt(info[1]);
		opcode = Integer.parseInt(info[2], 16);
		numberOfOperand = Integer.parseInt(info[3]);
	}
}
