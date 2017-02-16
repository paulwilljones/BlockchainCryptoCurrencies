import java.security.PublicKey;
import java.util.ArrayList;

public class TxHandler {

    public UTXOPool publicLedger;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool publicLedger)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.publicLedger = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output

     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

        ArrayList<Transaction.Input> tx_in = tx.getInputs();
        double sum_in = 0;
        ArrayList<UTXO> claim_utxos = new ArrayList<>();
        for (int i = 0; i < tx_in.size(); i++) {
            Transaction.Input in = tx_in.get(i);
            UTXO u = new UTXO(in.prevTxHash, in.outputIndex);

            // (1)
            if (!this.publicLedger.contains(u))
                return false;

            // (3)
            if (claim_utxos.contains(u))
                return false;
            claim_utxos.add(u);

            Transaction.Output output = this.publicLedger.getTxOutput(u);

            PublicKey pk = output.address;
            byte[] msg = tx.getRawDataToSign(i);

            // (2)
            if (!Crypto.verifySignature(pk, msg, in.signature))
                return false;
            sum_in += output.value;
        }

        ArrayList<Transaction.Output> tx_out = tx.getOutputs();
        double sum_out = 0;

        for (int i = 0; i < tx_out.size(); i++) {
            double v = tx_out.get(i).value;
            // (4)
            if (v < 0) return false;
            sum_out += v;
        }
        // (5)
        if (sum_in < sum_out)
            return false;

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> v_Tx = new ArrayList<>();

        for(Transaction tx: possibleTxs)
            if(isValidTx(tx)){
                //remove claimed output
                for(Transaction.Input in: tx.getInputs()){
                    UTXO u = new UTXO(in.prevTxHash, in.outputIndex);
                    this.publicLedger.removeUTXO(u);
                }

                //add in new output
                int out_index = 0;
                for(Transaction.Output out: tx.getOutputs()){
                    UTXO u = new UTXO(tx.getHash(), out_index++);
                    this.publicLedger.addUTXO(u, out);
                }

                v_Tx.add(tx);
            }

            int tx_size = v_Tx.size();
            Transaction [] valid_Txs = new Transaction[tx_size];
            for(int i = 0; i<tx_size; i++)
                valid_Txs[i] = v_Tx.get(i);

        return valid_Txs;
    }

}
