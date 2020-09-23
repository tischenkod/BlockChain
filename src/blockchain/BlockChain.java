package blockchain;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockChain implements Serializable {
    private static final BlockChain instance = new BlockChain(0);
    private final Object zeroCountSync = new Object();
    private final List<Transaction> transactions = new LinkedList<>();
    private long lastId = 0;
    private long lastWinner = -1;

    public static BlockChain getInstance() {
        return instance;
    }


    LinkedList<Block> chain = new LinkedList<>();
    int zeroCount;

    private BlockChain(int zeroCount) {
        this.zeroCount = zeroCount;
    }

    private static Stream<? extends Map.Entry<Long, Long>> splitTransaction(Transaction transaction) {
        if (transaction.from == null)
            return Map.of(transaction.to, transaction.amount).entrySet().stream();
        return Map.of(transaction.from,
                -1 * transaction.amount,
                transaction.to,
                transaction.amount).entrySet().stream();
    }

    public int getZeroCount() {
        synchronized (zeroCountSync) {
            return zeroCount;
        }
    }

    public synchronized Block getLastBlock() {
        return chain.size() == 0 ? null : chain.getLast();
    }

    public synchronized long getLastWinner() {
        return lastWinner;
    }

    public synchronized void setLastWinner(long lastWinner) {
        this.lastWinner = lastWinner;
    }

    public void add(long minerId, Block block) {
        synchronized (this) {
            if (validate(block)) {
                transactions.removeAll(block.getTransactions());
                setLastWinner(minerId);
                chain.add(block);
                if (block.getCalcTime() < 5000 && zeroCount < 3) {
                    zeroCount++;
                    block.setZeroCountDiff(1);
                } else if (block.getCalcTime() > 10000 && zeroCount > 0) {
                    zeroCount--;
                    block.setZeroCountDiff(-1);
                } else {
                    block.setZeroCountDiff(0);
                }
            }
        }
    }

    private synchronized boolean validate(Block block) {
        Block lastBlock = chain.size() == 0 ? null : chain.getLast();
        if (block.validate(lastBlock, zeroCount)) {
            long lastId = lastBlock == null ? -1 : lastBlock.maxTransactionId();
            for (Transaction transaction :
                    block.getTransactions()) {
                if (transaction.getId() <= lastId) {
                    return false;
                }
                lastId = transaction.getId();
            }

            Set<Long> payers = block.getTransactions().stream()
                    .map(t -> t.from)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, Long> amounts = Stream.concat( chain.stream(), Stream.of(block))
                    .flatMap(b -> b.getTransactions().stream())
                    .filter(transaction -> payers.contains(transaction.from) || payers.contains(transaction.to))
                    .flatMap(BlockChain::splitTransaction)
                    .filter(mapEntry -> payers.contains(mapEntry.getKey()))
                    .collect(Collectors.groupingBy(Map.Entry::getKey,
                            Collectors.summingLong(Map.Entry::getValue)));
            //noinspection SimplifyStreamApiCallChains
            return !amounts.values().stream().anyMatch(amount -> amount < 0);
        }
        return false;
    }

    public int length() {
        return chain.size();
    }

    public synchronized List<Transaction> getTransactions() {
        return new LinkedList<>(transactions);
    }

    public synchronized void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public long getNewId() {
        return lastId++;
    }
}
