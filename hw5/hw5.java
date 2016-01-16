//Kyle Kodani
//with assistance from: http://docs.oracle.com/javase/7/docs/api/, piazza


// import lists and other data structures from the Java standard library
import java.util.*;

// a type for arithmetic expressions
interface AExp {
    double eval(); 	                       // Problem 1a
    List<Sopn> toRPN(); 	               // Problem 1c
}

class Num implements AExp{
	protected double d;
	public Num(double d){
		this.d=d;
	}
	
	public double eval(){
		return d;
	}
	
	public List<Sopn> toRPN(){
		List<Sopn> instrs=new LinkedList<Sopn>();
		instrs.add(new Push(d));
		return instrs;
	}
	
	public String toString(){
		return ""+d;
	}
}

class BinOp implements AExp{
	protected AExp n1, n2;
	protected Op o;
	public BinOp(AExp n1, Op o, AExp n2){
		this.n1=n1;
		this.o=o;
		this.n2=n2;
	}
	
	public double eval(){
		return o.calculate(n1.eval(), n2.eval());
	}
	
	public List<Sopn> toRPN(){
		List<Sopn> instrs=new LinkedList<Sopn>();
		instrs.addAll(n1.toRPN());
		instrs.addAll(n2.toRPN());
		instrs.add(new Calculate(o));
		return instrs;
	}
	
	public String toString(){
		return ""+n1+" "+n2+" "+o;
	}
}

// a representation of four arithmetic operators
enum Op {
    PLUS { public double calculate(double a1, double a2) { return a1 + a2; } },
    MINUS { public double calculate(double a1, double a2) { return a1 - a2; } },
    TIMES { public double calculate(double a1, double a2) { return a1 * a2; } },
    DIVIDE { public double calculate(double a1, double a2) { return a1 / a2; } };

    abstract double calculate(double a1, double a2);
}

// a type for stack operations
interface Sopn {
	void eval(Stack<Double> stk);
}

class Push implements Sopn{
	double d;
	public Push(double d){
		this.d=d;
	}
	
	public void eval(Stack<Double> stk){
		stk.push(d);
	}
	
	public String toString(){
		return "PUSH "+d;
	}
}

class Swap implements Sopn{
	public Swap(){
		
	}
	
	public void eval(Stack<Double> stk){
		double d1=stk.pop();
		double d2=stk.pop();
		stk.push(d1);
		stk.push(d2);
	}
	
	public String toString(){
		return "SWAP";
	}
}

class Calculate implements Sopn{
	protected Op o;
	public Calculate(Op o){
		this.o=o;
	}
	
	public void eval(Stack<Double> stk){
		double d1=stk.pop();
		double d2=stk.pop();
		double v=o.calculate(d2,d1);
		stk.push(v);
	}
	
	public String toString(){
		return "CALCULATE "+o;
	}
}

// an RPN expression is essentially a wrapper around a list of stack operations
class RPNExp {
    protected List<Sopn> instrs;

    public RPNExp(List<Sopn> instrs) { this.instrs = instrs; }

	public double eval() {				// Problem 1b
		Sopn current;
		Stack<Double> stk=new Stack<Double>();
		for(Sopn s : instrs){
			s.eval(stk);
		}
		return stk.pop();
	}
}


class CalcTest {
    public static void main(String[] args) {
	    // a test for Problem 1a
	AExp aexp =
		new BinOp(new BinOp(new Num(1.0), Op.PLUS, new Num(2.0)),
			Op.TIMES,
			new Num(3.0));
	System.out.println("aexp evaluates to " + aexp.eval()); // aexp evaluates to 9.0

		
	// a test for Problem 1b
	List<Sopn> instrs = new LinkedList<Sopn>();
	instrs.add(new Push(1.0));
	instrs.add(new Push(2.0));
	instrs.add(new Calculate(Op.PLUS));
	instrs.add(new Push(3.0));
	instrs.add(new Swap());
	instrs.add(new Calculate(Op.TIMES));
	
	instrs.add(new Push(1.0));
	instrs.add(new Push(2.0));
	instrs.add(new Swap());
	instrs.add(new Calculate(Op.PLUS));
	instrs.add(new Push(3.0));
	instrs.add(new Swap());
	instrs.add(new Calculate(Op.MINUS));
	instrs.add(new Push(2.0));
	instrs.add(new Calculate(Op.PLUS));
	instrs.add(new Push(3.0));
	instrs.add(new Calculate(Op.PLUS));
	instrs.add(new Swap());
	instrs.add(new Calculate(Op.TIMES));
	
	RPNExp rpnexp = new RPNExp(instrs);
	System.out.println("rpnexp evaluates to " + rpnexp.eval());  // rpnexp evaluates to 9.0
	
		
	// a test for Problem 1c
	System.out.println("aexp converts to " + aexp.toRPN());
	System.out.println(aexp);
	RPNExp conv=new RPNExp(aexp.toRPN());
	System.out.println("aexp converts to " + aexp.toRPN());
	System.out.println("toRPN of aexp evaluates to " + conv.eval());
    }
}


interface Dict<K,V> {
    void put(K k, V v);
    V get(K k) throws NotFoundException;
}

class NotFoundException extends Exception {}


// Problem 2a
class DictImpl2<K,V> implements Dict<K,V> {
    protected Node<K,V> root;

    DictImpl2() {
		root=new Empty<K,V>();
	}

	//if no node with value k, new node is created and bcomes new root
	//points to former root as its next
    public void put(K k, V v) {
		if (root.put(k,v)==false) {
			Node<K,V> temp=new Entry<K,V>(k,v,root);
			root=temp;
		}
	}

    public V get(K k) throws NotFoundException {
		return root.get(k);

	}
}

interface Node<K,V> {
	boolean put(K k,V v);
	V get(K k) throws NotFoundException;
}

class Empty<K,V> implements Node<K,V> {
    Empty() {}
	
	public boolean put(K k,V v){
		return false;
	}
	
	public V get(K k) throws NotFoundException{
		throw new NotFoundException();
	}
}

class Entry<K,V> implements Node<K,V> {
    protected K k;
    protected V v;
    protected Node<K,V> next;

    Entry(K k, V v, Node<K,V> next) {
		this.k = k;
		this.v = v;
		this.next = next;
    }
	
	public boolean put(K k,V v){
		if (this.k.equals(k)) {
			this.v=v;
			return true;
		}
		else {
			return next.put(k,v);
		}
	}
	
	public V get(K k) throws NotFoundException{
		if(this.k.equals(k)) {
			return v;
		}
		else {
			return next.get(k);
		}

	}
}


interface DictFun<A,R> {
    R invoke(A a) throws NotFoundException;
}

// Problem 2b
class DictImpl3<K,V> implements Dict<K,V> {
    protected DictFun<K,V> dFun;

	//creates new dict that is empty, immediately throws NotFoundException
    DictImpl3() {
		dFun=new DictFun<K,V>(){
			public V invoke(K k) throws NotFoundException{
				throw new NotFoundException();
			}
		};
	}
	
	//creates new dict that adds K,V
	//upon invoking this new dictionary, it looks for k in itself and in dFun
    public void put(final K k, final V v) {
		final DictFun<K,V> temp=dFun;
		dFun=new DictFun<K,V>(){
			protected K thisk=k;
			protected V thisv=v;
			
			public V invoke(K k) throws NotFoundException{
				if (thisk.equals(k)) {
					return thisv;
				}
				else {
					return temp.invoke(k);
				}
			}
		};
	}

    public V get(K k) throws NotFoundException {
		try{
			return dFun.invoke(k);
		} catch(NotFoundException e) {
			throw new NotFoundException();
		}
	}
}


class Pair<A,B> {
    protected A fst;
    protected B snd;

    Pair(A fst, B snd) { this.fst = fst; this.snd = snd; }

    A fst() { return fst; }
    B snd() { return snd; }
}

// Problem 2c
interface FancyDict<K,V> extends Dict<K,V> {
    void clear();
    boolean containsKey(K k);
    void putAll(List<Pair<K,V>> entries);
}

class FancyDictImpl2<K,V> extends DictImpl2<K,V> implements FancyDict<K,V>{
	public void clear(){
		root=new Empty<K,V>();
	}
	
	//if root.get(k) doesnt throw NotFoundException then we know
	//that k is in dict
	public boolean containsKey(K k){
		try {
			root.get(k);
			return true;
		}
		catch (NotFoundException e) {
			return false;
		}
	}
	
	public void putAll(List<Pair<K,V>> entries){
		for(Pair<K,V> p : entries) {
			super.put(p.fst(), p.snd());
		}
	}
}

class DictTest {
    public static void main(String[] args) {

	
	// a test for Problem 2a
	Dict<String,Integer> dict1 = new DictImpl2<String,Integer>();
	dict1.put("hello", 23);
	dict1.put("bye", 45);
	//dict1.put("bye", 12);
	try {
	    System.out.println("bye maps to " + dict1.get("bye")); // prints 45
	    System.out.println("hi maps to " + dict1.get("hi"));  // throws an exception
	} catch(NotFoundException e) {
	    System.out.println("not found!");  // prints "not found!"
	}
	

	
	// a test for Problem 2b
	Dict<String,Integer> dict2 = new DictImpl3<String,Integer>();
	dict2.put("hello", 23);
	dict2.put("bye", 45);
	//dict2.put("bye", 12);
	try {
	    System.out.println("bye maps to " + dict2.get("bye"));  // prints 45
	    System.out.println("hi maps to " + dict2.get("hi"));   // throws an exception
	} catch(NotFoundException e) {
	    System.out.println("not found!");  // prints "not found!"
	}
	

	// a test for Problem 2c
	FancyDict<String,Integer> dict3 = new FancyDictImpl2<String,Integer>();
	dict3.put("hello", 23);
	dict3.put("bye", 45);
	
	List<Pair<String, Integer>> entries= new LinkedList<Pair<String, Integer>>();
	entries.add(new Pair("a",1));
	entries.add(new Pair("b",2));
	entries.add(new Pair("c",3));
	entries.add(new Pair("d",4));
	dict3.putAll(entries);
	try {
		System.out.println("bye maps to " + dict3.get("bye")); // prints 45
		System.out.println("hi maps to " + dict3.get("hi"));  // throws an exception
	} catch(NotFoundException e) {
		System.out.println("not found!");  // prints "not found!"
	}
	
	System.out.println(dict3.containsKey("bye")); // prints true
	dict3.clear();
	System.out.println(dict3.containsKey("bye")); // prints false

    }
}
