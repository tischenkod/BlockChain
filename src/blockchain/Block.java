package blockchain;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.security.MessageDigest;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.String.format;

class StringUtil {
    /* Applies Sha256 to a string and returns a hash. */
    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}

public class Block implements Serializable {
    private final List<Transaction> transactions;
    private final long minerId;
    private final long id;
    private final long timeStamp = new Date().getTime();
    private final String prevHash;
    private String hash;
    private final int zeroCount;
    private long magicNumber;
    private long calcTime;
    private int zeroCountDiff;

    public Block(long minerId, Block prevBlock, int zeroCount, List<Transaction> transactions) {
        this.minerId = minerId;
        this.transactions = transactions;
        if (prevBlock != null)
        {
            this.id = prevBlock.id + 1;
            this.prevHash = prevBlock.hash;
        } else {
            this.id = 1;
            this.prevHash = null;
        }

        this.zeroCount = zeroCount;
        this.magicNumber = 0;
        rehash();
    }

    public long getCalcTime() {
        return calcTime;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setZeroCountDiff(int zeroCountDiff) {
        this.zeroCountDiff = zeroCountDiff;
    }

    public void rehash() {
        this.hash = calculateHash();
    }

    @Override
    public String toString() {
        String nAction;
        switch (zeroCountDiff) {
            case -1:
                nAction = "N was decreased by 1";
                break;
            case 1:
                nAction = "N was increased to " + (zeroCount + 1);
                break;
            case 0:
                nAction = "N stays the same";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + zeroCountDiff);
        }
        return format("Block:%n" +
                        "Created by: miner%d%n" +
                        "miner%1$d gets 100 VC%n" +
                        "Id: %d%n" +
                        "Timestamp: %d%n" +
                        "Magic number: %d%n" +
                        "Hash of the previous block: %n%s%n" +
                        "Hash of the block:%n%s%n" +
                        "Block data:%n%s" +
                        "Block was generating for %f seconds%n%s%n",
                minerId,
                id,
                timeStamp,
                magicNumber,
                prevHash == null ? "0" : prevHash,
                hash,
                transactionsString(),
                (double)calcTime / 1000,
                nAction);
    }

    private String transactionsString() {
        if (transactions.size() <= 1) {
            return "No transactions\n";
        } else {
            return transactions.stream()
                    .limit(transactions.size() - 1)
                    .map(msg -> msg + "\n")
                    .collect(Collectors.joining());
        }
    }

    public String toStringForHashing() {
        return format("Id: %d%n" +
                "Timestamp: %d%n" +
                "Magic number: %d%n" +
                "Hash of the previous block: %n%s%s",
                id,
                timeStamp,
                magicNumber,
                prevHash == null ? "0" : prevHash,
                messagesStringWithSign());
    }

    private String messagesStringWithSign() {
        if (transactions.size() == 0) {
            return " no messages\n";
        } else {
            StringBuilder string = new StringBuilder();
            for (Transaction msg :
                    transactions) {
                string.append(msg).append("\n");
            }
            return string.toString();
        }
    }

    public String calculateHash() {
        long startTime = new Date().getTime();
        BlockChain blockChain = BlockChain.getInstance();
        while (true) {
            magicNumber = new Random().nextLong();
            String hash = StringUtil.applySha256(this.toStringForHashing());
            //noinspection SimplifyStreamApiCallChains
            if (!hash.chars().limit(zeroCount).parallel().anyMatch(c -> c != '0')) {
                calcTime = new Date().getTime() - startTime;
                return hash;
            }
            if (blockChain.getLastBlock().id >= id)
                return null;
        }
    }

    public boolean validate(Block lastBlock, int zeroCount) {
        return (lastBlock == null ||
                (lastBlock.id == id - 1 && lastBlock.hash.equals(prevHash))) &&
                hash != null &&
                hash.equals(StringUtil.applySha256(this.toStringForHashing())) &&
                (zeroCount == 0 ||
                        hash.matches(format("^0{%d,}.*", zeroCount)));
    }

    public long maxTransactionId() {
        long maxId = -1;
        for (Transaction transaction :
                transactions) {
            maxId = Math.max(maxId, transaction.getId());
        }
        return maxId;
    }
}
