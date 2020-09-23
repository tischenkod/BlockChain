package blockchain;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class Miner extends Thread{
    private static int curId = 0;
    private final long id;

    PrivateKey privateKey;
    PublicKey publicKey;

    Transaction pay(Long from,Long to, long amount, long transactionId) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        return new Transaction(from, to, amount, transactionId, privateKey);
    }


    public Miner() throws NoSuchProviderException, NoSuchAlgorithmException {
        super();
        id = ++curId;

        GenerateKeys gk = new GenerateKeys(1024);
        gk.createKeys();
        privateKey = gk.getPrivateKey();
        publicKey = gk.getPublicKey();
    }

    @Override
    public void run() {
        BlockChain blockChain = BlockChain.getInstance();
        while (!interrupted()) {
            Block lastBlock;
            List<Transaction> transactions;
            synchronized (blockChain) {
                transactions = blockChain.getTransactions();
                lastBlock = blockChain.getLastBlock();
            }
            try {
                transactions.add(pay(null, id, 100, blockChain.getNewId()));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
                e.printStackTrace();
                continue;
            }
            blockChain.add(id, new Block(id, lastBlock, blockChain.getZeroCount(), transactions));
        }
    }
}
