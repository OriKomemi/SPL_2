package bgu.spl.mics;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */

public class MessageBusImpl implements MessageBus {

    private static class MessageBusHolder{
		private static volatile MessageBusImpl instance = new MessageBusImpl();;
	}

    public static MessageBusImpl getInstance() {
		return MessageBusHolder.instance;
	}

    private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> queues;
    private final ConcurrentHashMap<Class<? extends Message>, CopyOnWriteArrayList<MicroService>> subscriptionsEvents;
    private final ConcurrentHashMap<Class<? extends Message>, CopyOnWriteArrayList<MicroService>> subscriptionsBroadcast;

    private final ConcurrentHashMap<Class<? extends Event<?>>, Queue<MicroService>> roundRobinQueues;
    private final ConcurrentHashMap<Event<?>, Future<?>> futures;

    private MessageBusImpl() {
        queues = new ConcurrentHashMap<>();
        subscriptionsEvents = new ConcurrentHashMap<>();
        subscriptionsBroadcast = new ConcurrentHashMap<>();
        roundRobinQueues = new ConcurrentHashMap<>();
        futures = new ConcurrentHashMap<>();
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        subscriptionsEvents.putIfAbsent(type, new CopyOnWriteArrayList<>());
        subscriptionsEvents.get(type).add(m);
        roundRobinQueues.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        roundRobinQueues.get(type).add(m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        subscriptionsBroadcast.putIfAbsent(type, new CopyOnWriteArrayList<>());
        subscriptionsBroadcast.get(type).add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        @SuppressWarnings("unchecked")
        Future<T> future = (Future<T>) futures.get(e);
        if (future != null) {
            future.resolve(result);
            futures.remove(e);
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        List<MicroService> subscribers = subscriptionsBroadcast.getOrDefault(b.getClass(), new CopyOnWriteArrayList<>());
        for (MicroService m : subscribers) {
            BlockingQueue<Message> queue = queues.get(m);
            if (queue != null) {
                queue.offer(b);
            }
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Queue<MicroService> queue = roundRobinQueues.get(e.getClass());
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        MicroService m;
        synchronized (queue) {
            m = queue.poll();
            if (m != null) {
                queue.add(m);
            }
        }
        if (m == null) {
            return null;
        }
        BlockingQueue<Message> serviceQueue = queues.get(m);
        if (serviceQueue == null) {
            return null;
        }
        Future<T> future = new Future<>();
        futures.put(e, future);
        serviceQueue.offer(e);
        return future;
    }

	@Override
    public synchronized void register(MicroService m) {
        queues.putIfAbsent(m, new LinkedBlockingQueue<>());
    }

    @Override
    public synchronized void unregister(MicroService m) {
        queues.remove(m);
        subscriptionsEvents.values().forEach(list -> list.remove(m));
        subscriptionsBroadcast.values().forEach(list -> list.remove(m));
        roundRobinQueues.values().forEach(queue -> queue.remove(m));
    }


    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        BlockingQueue<Message> queue = queues.get(m);
        if (queue == null) {
            throw new IllegalStateException("MicroService is not registered.");
        }
        return queue.take();
    }

    @Override
    public boolean isRegistered(MicroService m) {
        // In this design, 'queues' is the data structure that tracks registered microservices.
        return queues.containsKey(m);
    }
}
