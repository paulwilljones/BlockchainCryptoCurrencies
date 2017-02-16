// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private Block genesisBlock;
    private Block currentBlock;

    private TransactionPool transactionPool;
    private UTXOPool utxoPool = new UTXOPool();

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        this.genesisBlock = genesisBlock;
        this.currentBlock = this.genesisBlock;

        transactionPool = new TransactionPool();

        addBlockUtxos(this.genesisBlock);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return currentBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        if(!isValid(block)) return false;

        currentBlock = block;
        addBlockUtxos(currentBlock);
        transactionPool = new TransactionPool();

        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }

    public boolean isValid(Block block) {
        if (block.getPrevBlockHash() == null || block.getPrevBlockHash() != currentBlock.getHash()) return false;

        TxHandler txHandler = new TxHandler(getMaxHeightUTXOPool());

        for (Transaction t : block.getTransactions()) {
            if (!txHandler.isValidTx(t)) return false;
        }

        return true;
    }

    public void addBlockUtxos(Block block) {
        Transaction coinbase = block.getCoinbase();
        utxoPool.addUTXO(new UTXO(coinbase.getHash(), 0), coinbase.getOutput(0));
        for(Transaction t : block.getTransactions()) {
            for (int j = 0; j < t.getOutputs().size(); j++) {
                utxoPool.addUTXO(new UTXO(t.getHash(), j), t.getOutput(j));
                utxoPool.removeUTXO(new UTXO(t.getHash(), j));
            }
        }
    }
}