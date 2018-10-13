package co.rsk.db;

import co.rsk.trie.Trie;
import org.ethereum.vm.DataWord;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class ContractStorage {
    private Trie trie;

    public ContractStorage(Trie trie) {
        this.trie = trie;
    }

    public DataWord getValue(DataWord key) {
        byte[] value = this.trie.get(key.getData());

        if (value == null || value.length == 0) {
            return null;
        }

        return new DataWord(value);
    }
}
