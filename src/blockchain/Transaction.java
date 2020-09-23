package blockchain;

import blockchain.utils.ArrayUtils;
import blockchain.utils.Longs;

import java.security.*;

import static java.lang.String.format;

public class Transaction {
    final private long id;
    Long from;
    long to;
    long amount;
    byte[] signature;


    public Transaction(Long from, long to, long amount, long id, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.id = id;
        signature = sign(privateKey);
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        if (from == null) {
            return format("miner%d gets %d VC", to, amount);
        }
        return format("miner%d sent %d VC to miner%d", from, amount, to);
    }

    public byte[] dataToSign() {
        return ArrayUtils.addAll(new byte[][]{Longs.toByteArray(from == null ? 0L : from), Longs.toByteArray(to), Longs.toByteArray(amount), Longs.toByteArray(id)});
    }

    public byte[] sign(PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance("SHA1withRSA");
        rsa.initSign(privateKey);
        rsa.update(dataToSign());
        return rsa.sign();
    }

}