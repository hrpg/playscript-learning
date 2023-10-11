package stone.ast;
import java.util.Iterator;

public abstract class ASTree implements Iterable<ASTree> {

    // 返回第 i 个子节点
    public abstract ASTree child(int i);
    // 返回子节点的数量
    public abstract int numChildren();
    // 返回指向子节点集合的迭代器
    public abstract Iterator<ASTree> children();
    // 返回该节点 token的位置信息
    public abstract String location();

    // 返回子节点迭代器：实现Iterable接口必须要实现这个方法
    public Iterator<ASTree> iterator() {
        return children();
    }

}
