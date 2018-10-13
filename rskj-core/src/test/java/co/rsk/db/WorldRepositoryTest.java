package co.rsk.db;

import co.rsk.core.RskAddress;
import org.ethereum.core.AccountState;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class WorldRepositoryTest {
    @Test
    public void getUnknownAccountState() {
        WorldRepository repository = new WorldRepository();

        AccountState accountState = repository.getAccountState(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNull(accountState);
    }
}
