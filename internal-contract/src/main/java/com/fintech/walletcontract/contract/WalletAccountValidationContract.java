package com.fintech.walletcontract.contract;

import com.fintech.walletcontract.dto.WalletAccountValidationRequest;
import com.fintech.walletcontract.dto.WalletAccountValidationResponse;

public interface WalletAccountValidationContract {

    WalletAccountValidationResponse validateWalletAccount(WalletAccountValidationRequest walletAccountValidationRequest);
}
