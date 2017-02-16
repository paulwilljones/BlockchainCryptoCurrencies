import org.junit.Test;

import java.security.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BlockChainTest {

    @Test
    public void testEmptyAfterGenesisBlock() {
        Block genesisBlock = new Block(null, createKey().getPublic());
        genesisBlock.finalize();

        BlockChain chain = new BlockChain(genesisBlock);

        Block block = new Block(genesisBlock.getHash(), createKey().getPublic());

//        assertEquals("".getBytes(), block.getPrevBlockHash());
        assertEquals(block.getPrevBlockHash(), genesisBlock.getHash());

        assertTrue(chain.addBlock(block));
    }



    private KeyPair createKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}