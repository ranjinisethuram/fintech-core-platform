package com.fintech.transaction.feign.wallet;
import com.fintech.walletcontract.contract.WalletAccountValidationContract;
import com.fintech.walletcontract.dto.WalletAccountValidationRequest;
import com.fintech.walletcontract.dto.WalletAccountValidationResponse;
import org.springframework.stereotype.Component;

@Component
public class WalletValidationAdapter implements WalletAccountValidationContract {
    private final WalletValidationClientWrapper wrapper;

    public WalletValidationAdapter(WalletValidationClientWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public WalletAccountValidationResponse validateWalletAccount(
            WalletAccountValidationRequest walletAccountValidationRequest) {
        return wrapper.validateAccount(walletAccountValidationRequest).join();
    }
}
