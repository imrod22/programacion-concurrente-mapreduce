package bateria.catodo_li_s_benchmarking;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorMedidaAjuste {
	private final int capacity = 1000;
    private final Queue<String[]> data;
    private boolean isComplete;
    private final ReentrantLock lock;
    private final Condition notFull;
    private final Condition notEmpty;

    public MonitorMedidaAjuste() {
        this.data = new LinkedList<>();
        this.isComplete = false;
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
    }

    public void put(String[] item) throws InterruptedException {
        lock.lock();
        try {
            while (data.size() == capacity) {
                notFull.await();
            }
            data.offer(item);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public String[] take() throws InterruptedException {
        lock.lock();
        try {
            while (data.isEmpty() && !isComplete) {
                notEmpty.await();
            }
            if (data.isEmpty() && isComplete) {
                return null;
            }
            String[] item = data.poll();
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    public void setComplete() {
        lock.lock();
        try {
            isComplete = true;
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
