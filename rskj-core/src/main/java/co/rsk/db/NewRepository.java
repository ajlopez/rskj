package co.rsk.db;

import co.rsk.core.Coin;
import co.rsk.core.RskAddress;
import co.rsk.crypto.Keccak256;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;

/**
 * Created by ajlopez on 22/10/2018.
 */
public interface NewRepository {
    // Account query methods
    Coin getAccountBalance(RskAddress accountAddress);
    BigInteger getAccountNonce(RskAddress accountAddress);
    byte[] getAccountCode(RskAddress accountAddress);

    // Storage query methods
    DataWord getStorageValue(RskAddress accountAddress, DataWord key);
    byte[] getStorageBytes(RskAddress accountAddress, DataWord key);
}
