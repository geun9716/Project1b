import java.util.ArrayList;

/**
 * literal�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class LiteralTable {
	ArrayList<String> literalList = new ArrayList<String>();
	ArrayList<Integer> locationList= new ArrayList<Integer>() ;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	
	/**
	 * ���ο� Literal�� table�� �߰��Ѵ�.
	 * @param literal : ���� �߰��Ǵ� literal�� label
	 * @param location : �ش� literal�� ������ �ּҰ�
	 * ���� : ���� �ߺ��� literal�� putLiteral�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifyLiteral()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putLiteral(String literal, int location) {
		literalList.add(literal);
		locationList.add(location);
	}
	
	/**
	 * ������ �����ϴ� literal ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param literal : ������ ���ϴ� literal�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifyLiteral(String literal, int newLocation) {
		locationList.set(literalList.indexOf(literal), newLocation);
	}
	
	/**
	 * ���ڷ� ���޵� literal�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param literal : �˻��� ���ϴ� literal�� label
	 * @return literal�� ������ �ִ� �ּҰ�. �ش� literal�� ���� ��� -1 ����
	 */
	public int search(String literal) {
		int address = 0;
		int index;
		if ((index = literalList.indexOf(literal)) >= 0)
			address = locationList.get(index);
		else
			address = -1;
		
		return address;
	}
}
