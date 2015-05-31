package org.pimatic.model;

import android.accounts.Account;
import android.content.Context;

import org.pimatic.accounts.AccountGeneral;

/**
 * Created by h3llfire on 31.05.15.
 */
public class AccountManager {
    private Context context;
    private android.accounts.AccountManager accountManager;

    public AccountManager(Context context) {
        this.context = context;
        accountManager = android.accounts.AccountManager.get(context);
    }


    public String[] getAllAccountNames() {
        final Account availableAccounts[] = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        String names[] = new String[availableAccounts.length];
        for (int i = 0; i < availableAccounts.length; i++) {
            names[i] = availableAccounts[i].name;
        }
        return names;
    }

    public ConnectionOptions getConnectionFor(String name) {
        final Account availableAccounts[] = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        for (int i = 0; i < availableAccounts.length; i++) {
            if (availableAccounts[i].name.equals(name)) {
                Account account = availableAccounts[i];
                String url = accountManager.getUserData(account, AccountGeneral.ACCOUNT_USER_DATA_URL);
                return ConnectionOptions.fromAuthToken(url);
            }
            ;
        }
        return null;
    }


}
