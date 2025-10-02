package com.coheser.app.activitesfragments.payment.utils;

import androidx.annotation.NonNull;


public interface CreditCardNumberListener {

    void onChanged(@NonNull String number, @NonNull CreditCardBrand brand);
}
