package blockchain;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args)
            throws NoSuchProviderException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException,
            SignatureException {
        BlockChain blockChain = BlockChain.getInstance();

        final int minerCount = 4;
        final int chainLength = 15;

        Miner[] miners = new Miner[minerCount];

        for (int i = 0; i < minerCount; i++) {
            miners[i] = new Miner();
            miners[i].start();
        }

        while (blockChain.length() < chainLength) {
            int minerId = new Random().nextInt(minerCount - 1);
            long lastWinner = blockChain.getLastWinner();

            if (lastWinner >= 0) {
                if (minerId >= lastWinner) {
                    minerId++;
                }
                blockChain.addTransaction(miners[minerId].pay(lastWinner, (long) minerId, new Random().nextInt(4) + 1, blockChain.getNewId()));
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < minerCount; i++) {
            miners[i].interrupt();
        }

        for (int i = 0; i < chainLength; i++) {
            System.out.println(blockChain.chain.get(i));
        }
    }
}
