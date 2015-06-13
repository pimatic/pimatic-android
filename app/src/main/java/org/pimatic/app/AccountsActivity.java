
package org.pimatic.app;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.pimatic.accounts.AccountGeneral;
import org.pimatic.accounts.PimaticAccountAuthenticatorActivity;
import org.pimatic.model.ConnectionOptions;

import java.util.ArrayList;

public class AccountsActivity  extends Fragment {

    private static final String STATE_DIALOG = "state_dialog";
    private static final String STATE_INVALIDATE = "state_invalidate";
    private LinearLayout mLinearLayoutView;
    private String TAG = this.getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLinearLayoutView = (LinearLayout) inflater.inflate(
                R.layout.accounts, container, false);

        final ArrayList<String> accountNames = new ArrayList<>();
        final ListAccountAdapter adapter = new ListAccountAdapter(getActivity(), accountNames);

        final ListView listView = (ListView) mLinearLayoutView.findViewById(R.id.accountsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long arg3) {
                String accountName = adapter.getAccountName(position);
                Account account = org.pimatic.model.AccountManager.getInstance(AccountsActivity.this.getActivity()).getAccountByName(accountName);
                editAccount(account);
            }
        });

        mLinearLayoutView.findViewById(R.id.btnAddAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewAccount();
            }
        });

        return mLinearLayoutView;

    }

    /**
     * Add new account to the account manager
     */
    private void addNewAccount() {
        org.pimatic.model.AccountManager.getInstance(getActivity()).addNewAccount(this.getActivity(), new AccountManagerCallback<Bundle>() {
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
        });
    }

    private void editAccount(final Account account) {
        final Intent intent = new Intent(AccountsActivity.this.getActivity(), PimaticAccountAuthenticatorActivity.class);
        intent.putExtra(PimaticAccountAuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(PimaticAccountAuthenticatorActivity.ARG_AUTH_TYPE, AccountGeneral.AUTHTOKEN_TYPE_CONNECTION_URL);
        intent.putExtra(PimaticAccountAuthenticatorActivity.ARG_ACCOUNT_NAME, account.name);
        ConnectionOptions conOps = org.pimatic.model.AccountManager.getInstance(getActivity()).getConnectionFor(account.name);
        conOps.putInIntent(intent);
        startActivity(intent);
    }


    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity().getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}