import java.sql.SQLSyntaxErrorException;

public class FixedMaxSizeDeque<T>{
    private int l=-1,r=-1;

    private T elements[];
    private int max_size;

    public int getMaxSize() {
        return max_size;
    }
    public FixedMaxSizeDeque(T elements[]){
        this.max_size=elements.length;
        this.elements=elements;
        for(int i=0;i<max_size;++i){
            elements[i]=null;
        }
    }
    public void addElement(T what){
        if(l==-1 && r==-1){
            r=0;
            l=0;
        }else {
            r = (r + 1) % max_size;
        }
        elements[r] = what;
    }
    public int size(){
        if(r==-1 && l==-1){
            return 0;
        }
        if(l<=r){
            return r-l+1;
        }
        return r+1 + max_size-l;
    }
    public T getElement(int ind){
        if(ind>=size()){
            return null;
        }
        if(r<=l){
            return elements[l+ind];
        }else{
            int seg_end = max_size-l;
            if(ind <= seg_end){
                return elements[l+ind];
            }else{
                ind -= seg_end;
                return elements[ind];
            }
        }
    }
    public T getFirst(){
        return elements[l];
    }
    public T getLast(){
        return elements[r];
    }
    public void removeFirst(){
        elements[l] = null;
        if(size()==1){
            l=-1;
            r=-1;
        }else {
            l = (l + 1) % max_size;
        }
    }
    public void removeLast(){
        elements[r] = null;
        if(size()==1){
            l=-1;
            r=-1;
        }else {
            r = (r - 1) % max_size;
        }
    }

    public static void main(String[] args){
        FixedMaxSizeDeque<Integer> siea = new FixedMaxSizeDeque<>(new Integer[7]);
        siea.addElement(1);
        siea.addElement(2);
        siea.addElement(3);
        siea.addElement(4);
        siea.addElement(5);
        System.out.println("Deque curr size: "+siea.size());
        for(int ind=0;ind<siea.size();++ind){
            System.out.println("Ind "+ind+": "+siea.getElement(ind));
        }
        System.out.println(        );
        while(siea.size()>1){
            System.out.print(siea.getFirst()+" ");
            siea.removeFirst();
            System.out.println(siea.getFirst());
            siea.removeFirst();
            siea.addElement(42);
        }

    }

}