import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This program demonstrates a scenario where a data structure can be modified and/or read by multiple threads simultaneously,
 * without undefined behavior, race conditions, exceptions, thanks to the logic of the program.
 * No language-provided synchronization mechanisms are implemented.
 **/
public class AddAndPollMultithreaded {


    /** This Queue is the main character here.
     * Java did not intend it to be perfectly thread safe.
     */
    static final Queue<Long> foo = new LinkedList<>();
    public static long startTime=-1;

    /** This thread has only 1 purpose: Adding elements to the queue in random moments **/
    static Thread a = new Thread(() -> {
        while(true){
            long curr = System.currentTimeMillis()-startTime;
            foo.add(curr);
            System.out.printf("Adding: %d %n",curr);

            try{
                Thread.sleep(ThreadLocalRandom.current().nextInt(0,50));
            }catch(InterruptedException ignored){}
        }
    });

    /** Every ~1s this thread checks the content of this queue.
     * Checking occurs at the very beginning of every iteration.
     * It then prints out all the elements that were present at the moment of checking and removes them.
     */
    static Thread b = new Thread(() -> {
        while(true){
            int k = foo.size(); // checking happens here.
            System.out.printf("Current size: %d %n",k);
            while(k>0){
                assert(foo.peek() != null);
                System.out.printf("%d ",foo.poll());  // only the first k elements are polled - these are the ones present in the queue at the moment of checking.
                --k;
            }
            System.out.println();
            try{
                Thread.sleep(1000);
            }catch(InterruptedException ignored){}
        }
    });

    /** This is actually pretty useful in a simple multiplayer game **/
    public static void main(String... args){
        startTime = System.currentTimeMillis();
        a.start();
        b.start();
    }
}

/* TWO IMPORTANT CHANGES: the adding delay and order of printing (thread a)
 * First of all, it should now be possible to see a situation where an element gets added while the queue is being read.
 * Despite that, the newly added element is not printed out as part of the queue in the reading process and there's no error.
 * Additionally, in thread "a", the element is first added and then the user gets notified about that.
 * Before that change, it was possible to see a message: "Adding: xxxx" and then the reading of the queue would get called and xxxx would not appear.
 * That would be because xxxx hadn't been added yet and the reading was called in between System.out.println("Adding...") and the actual adding.
 */
