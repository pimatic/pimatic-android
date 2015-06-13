package org.pimatic.model;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.pimatic.accounts.AccountGeneral;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class AccountManager extends UpdateEventEmitter<AccountManager.UpdateListener> {

    private android.accounts.AccountManager accountManager;

    private static AccountManager singleton;
    public static AccountManager getInstance(Context context) {
        if(singleton == null) {
            singleton = new AccountManager(context);
        }
        return singleton;
    }

    /**
     * Initializes the static AccountManager
     * @param context
     */
    private AccountManager(Context context) {
        accountManager = android.accounts.AccountManager.get(context);
    }

   /**
    * Returns all account names in the form "user@host" that are in the android account manager.
    */
    public String[] getAllAccountNames() {
        final Account availableAccounts[] = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        String names[] = new String[availableAccounts.length];
        for (int i = 0; i < availableAccounts.length; i++) {
            names[i] = availableAccounts[i].name;
        }
        return names;
    }

    public Account getAccountByName(String accountName) {
        final Account availableAccounts[] = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        for (int i = 0; i < availableAccounts.length; i++) {
            if (availableAccounts[i].name.equals(accountName)) {
                return availableAccounts[i];
            }
        }
        return null;
    }

    /**
     * Get the Connection details for a AccountName
     * @param accountName account name in the form of "user@host"
     * @return ConnectionOptions that can be used to establish a connection
     */
    public ConnectionOptions getConnectionFor(final String accountName) {
        final Account account = getAccountByName(accountName);
        if(account == null) {
            return null;
        }
        String url = accountManager.getUserData(account, AccountGeneral.ACCOUNT_USER_DATA_URL);
        return ConnectionOptions.fromAuthToken(url);
    }

    /**
     * Add new account to the account manager
     */
    public AccountManagerFuture<Bundle> addNewAccount(final Activity activity, final AccountManagerCallback<Bundle> callback) {
        return accountManager.addAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_CONNECTION_URL, null, null, activity, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                AccountManager.this.didChange();
                callback.run(future);
            }
        }, null);
    }

    public interface UpdateListener extends UpdateEventEmitter.UpdateListener {
       void onChange();
    }
}
