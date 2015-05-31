
package org.pimatic.app;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.pimatic.accounts.AccountGeneral;
import org.pimatic.accounts.PimaticAccountAuthenticatorActivity;
import org.pimatic.model.ConnectionOptions;

public class AccountsActivity extends Activity {

    private static final String STATE_DIALOG = "state_dialog";
    private static final String STATE_INVALIDATE = "state_invalidate";

    private String TAG = this.getClass().getSimpleName();
    private AccountManager mAccountManager;
    private AlertDialog mAlertDialog;
    private boolean mInvalidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.accounts);
        mAccountManager = AccountManager.get(this);

        final Account availableAccounts[] = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        String names[] = new String[availableAccounts.length];
        for (int i = 0; i < availableAccounts.length; i++) {
            names[i] = availableAccounts[i].name;
        }

        final ArrayAdapter<String> accounts = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        ListView listView = (ListView) findViewById(R.id.accountsList);
        listView.setAdapter(accounts);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                final Intent intent = new Intent(AccountsActivity.this, PimaticAccountAuthenticatorActivity.class);
                Account account = availableAccounts[position];
                intent.putExtra(PimaticAccountAuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type);
                intent.putExtra(PimaticAccountAuthenticatorActivity.ARG_AUTH_TYPE, AccountGeneral.AUTHTOKEN_TYPE_CONNECTION_URL);
                intent.putExtra(PimaticAccountAuthenticatorActivity.ARG_ACCOUNT_NAME, account.name);
                ConnectionOptions conOps = ConnectionOptions.fromAuthToken(mAccountManager.getUserData(account, AccountGeneral.ACCOUNT_USER_DATA_URL));
                conOps.putInIntent(intent);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnAddAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_CONNECTION_URL);
                finish();
            }
        });


    }

    private void setupAccountList() {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            outState.putBoolean(STATE_DIALOG, true);
            outState.putBoolean(STATE_INVALIDATE, mInvalidate);
        }
    }

    /**
     * Add new account to the account manager
     *
     * @param accountType
     * @param authTokenType
     */
    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    showMessage("Account was created");
                    Log.d("udinic", "AddNewAccount Bundle is " + bnd);

                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }, null);
    }


    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}