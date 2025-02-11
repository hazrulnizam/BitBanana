package app.michaelwuensch.bitbanana.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.michaelwuensch.bitbanana.R;
import app.michaelwuensch.bitbanana.connection.manageNodeConfigs.NodeConfigsManager;
import app.michaelwuensch.bitbanana.contacts.ContactDetailsActivity;
import app.michaelwuensch.bitbanana.contacts.ContactsManager;
import app.michaelwuensch.bitbanana.contacts.ManageContactsActivity;
import app.michaelwuensch.bitbanana.customView.BSDScrollableMainView;
import app.michaelwuensch.bitbanana.lightning.LightningNodeUri;
import app.michaelwuensch.bitbanana.util.Wallet;


public class ChooseNodeActionBSDFragment extends BaseBSDFragment {

    private static final String LOG_TAG = ChooseNodeActionBSDFragment.class.getSimpleName();
    private static final String EXTRA_NODE_URI = "EXTRA_NODE_URI";

    private LightningNodeUri mNodeUri;

    public static ChooseNodeActionBSDFragment createChooseActionDialog(LightningNodeUri nodeUri) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NODE_URI, nodeUri);
        ChooseNodeActionBSDFragment chooseNodeActionBSDFragment = new ChooseNodeActionBSDFragment();
        chooseNodeActionBSDFragment.setArguments(intent.getExtras());
        return chooseNodeActionBSDFragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bsd_node_action, container);

        Bundle args = getArguments();
        mNodeUri = (LightningNodeUri) args.getSerializable(EXTRA_NODE_URI);

        BSDScrollableMainView bsdScrollableMainView = view.findViewById(R.id.scrollableBottomSheet);
        View btnAddContact = view.findViewById(R.id.addContactBtn);
        View btnSendMoney = view.findViewById(R.id.sendMoneyBtn);
        View btnOpenChannel = view.findViewById(R.id.openChannelBtn);
        View firstOrLine = view.findViewById(R.id.firstOrLine);

        bsdScrollableMainView.setOnCloseListener(this::dismiss);
        bsdScrollableMainView.setTitleIconVisibility(true);
        bsdScrollableMainView.setTitle(R.string.choose_action);

        if (NodeConfigsManager.getInstance().hasAnyConfigs()) {
            Wallet.getInstance().fetchNodeInfoFromLND(mNodeUri.getPubKey(), false, true, null);
        }

        // Check if this node is already a contact.
        ContactsManager cm = ContactsManager.getInstance();
        if (cm.doesContactDataExist(mNodeUri.getPubKey())) {
            // hide add contact button
            btnAddContact.setVisibility(View.GONE);
            firstOrLine.setVisibility(View.GONE);
        }

        // Action when clicked on "Add Contact" Button
        btnAddContact.setOnClickListener(v -> {
            ContactsManager cm1 = ContactsManager.getInstance();
            cm1.showContactNameInputDialog(getActivity(), mNodeUri.getPubKey(), new ContactsManager.OnNameConfirmedListener() {
                @Override
                public void onNameAccepted() {
                    Intent intent = new Intent(getActivity(), ContactDetailsActivity.class);
                    intent.putExtra(ManageContactsActivity.EXTRA_CONTACT, cm1.getContactByContactData(mNodeUri.getPubKey()));
                    startActivityForResult(intent, 0);
                    dismiss();
                }

                @Override
                public void onCancelled() {

                }
            });

        });

        // Action when clicked on "Send Money" Button
        btnSendMoney.setOnClickListener(v -> {
            SendBSDFragment sendBSDFragment = SendBSDFragment.createKeysendDialog(mNodeUri.getPubKey());
            sendBSDFragment.show(getActivity().getSupportFragmentManager(), "sendBottomSheetDialog");
            dismiss();
        });

        // Action when clicked on "Open Channel" button
        btnOpenChannel.setOnClickListener(v -> {
            OpenChannelBSDFragment openChannelBSDFragment = new OpenChannelBSDFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(OpenChannelBSDFragment.ARGS_NODE_URI, mNodeUri);
            openChannelBSDFragment.setArguments(bundle);
            openChannelBSDFragment.show(getActivity().getSupportFragmentManager(), OpenChannelBSDFragment.TAG);
            dismiss();
        });

        return view;
    }
}
