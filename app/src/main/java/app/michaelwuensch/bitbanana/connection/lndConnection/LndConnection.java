package app.michaelwuensch.bitbanana.connection.lndConnection;


import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import app.michaelwuensch.bitbanana.connection.manageNodeConfigs.BBNodeConfig;
import app.michaelwuensch.bitbanana.connection.manageNodeConfigs.NodeConfigsManager;
import app.michaelwuensch.bitbanana.lnd.LndAutopilotService;
import app.michaelwuensch.bitbanana.lnd.LndChainKitService;
import app.michaelwuensch.bitbanana.lnd.LndChainNotifierService;
import app.michaelwuensch.bitbanana.lnd.LndInvoicesService;
import app.michaelwuensch.bitbanana.lnd.LndLightningService;
import app.michaelwuensch.bitbanana.lnd.LndPeersService;
import app.michaelwuensch.bitbanana.lnd.LndRouterService;
import app.michaelwuensch.bitbanana.lnd.LndSignerService;
import app.michaelwuensch.bitbanana.lnd.LndStateService;
import app.michaelwuensch.bitbanana.lnd.LndVersionerService;
import app.michaelwuensch.bitbanana.lnd.LndWalletKitService;
import app.michaelwuensch.bitbanana.lnd.LndWalletUnlockerService;
import app.michaelwuensch.bitbanana.lnd.LndWatchtowerClientService;
import app.michaelwuensch.bitbanana.lnd.LndWatchtowerService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndAutopilotService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndChainKitService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndChainNotifierService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndInvoicesService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndLightningService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndPeersService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndRouterService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndSignerService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndStateService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndVersionerService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndWalletKitService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndWalletUnlockerService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndWatchtowerClientService;
import app.michaelwuensch.bitbanana.lnd.RemoteLndWatchtowerService;
import app.michaelwuensch.bitbanana.tor.TorManager;
import app.michaelwuensch.bitbanana.util.BBLog;
import app.michaelwuensch.bitbanana.util.Wallet;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

/**
 * Singleton to handle the connection to lnd
 */
public class LndConnection {

    private static final String LOG_TAG = LndConnection.class.getSimpleName();

    private static LndConnection mLndConnectionInstance;

    private MacaroonCallCredential mMacaroon;
    private ManagedChannel mSecureChannel;
    private LndAutopilotService mLndAutopilotService;
    private LndChainKitService mLndChainKitService;
    private LndChainNotifierService mLndChainNotifierService;
    private LndInvoicesService mLndInvoicesService;
    private LndLightningService mLndLightningService;
    private LndPeersService mLndPeersService;
    private LndRouterService mLndRouterService;
    private LndSignerService mLndSignerService;
    private LndStateService mLndStateService;
    private LndVersionerService mLndVersionerService;
    private LndWalletKitService mLndWalletKitService;
    private LndWalletUnlockerService mLndWalletUnlockerService;
    private LndWatchtowerService mLndWatchtowerService;
    private LndWatchtowerClientService mLndWatchtowerClientService;
    private BBNodeConfig mConnectionConfig;
    private boolean isConnected = false;

    private LndConnection() {
        ;
    }

    public static synchronized LndConnection getInstance() {
        if (mLndConnectionInstance == null) {
            mLndConnectionInstance = new LndConnection();
        }
        return mLndConnectionInstance;
    }

    public LndAutopilotService getAutopilotService() {
        return mLndAutopilotService;
    }

    public LndChainKitService getChainKitService() {
        return mLndChainKitService;
    }

    public LndChainNotifierService getChainNotifierService() {
        return mLndChainNotifierService;
    }

    public LndInvoicesService getInvoicesService() {
        return mLndInvoicesService;
    }

    public LndLightningService getLightningService() {
        return mLndLightningService;
    }

    public LndPeersService getPeersService() {
        return mLndPeersService;
    }

    public LndRouterService getRouterService() {
        return mLndRouterService;
    }

    public LndSignerService getSignerService() {
        return mLndSignerService;
    }

    public LndStateService getStateService() {
        return mLndStateService;
    }

    public LndVersionerService getVersionerService() {
        return mLndVersionerService;
    }

    public LndWalletKitService getWalletKitService() {
        return mLndWalletKitService;
    }

    public LndWalletUnlockerService getWalletUnlockerService() {
        return mLndWalletUnlockerService;
    }

    public LndWatchtowerService getWatchtowerService() {
        return mLndWatchtowerService;
    }

    public LndWatchtowerClientService getWatchtowerClientService() {
        return mLndWatchtowerClientService;
    }

    private void readSavedConnectionInfo() {

        // Load current wallet connection config
        mConnectionConfig = NodeConfigsManager.getInstance().getCurrentNodeConfig();

        // Generate Macaroon
        mMacaroon = new MacaroonCallCredential(mConnectionConfig.getMacaroon());
    }

    private void generateChannelAndStubs() {
        String host = mConnectionConfig.getHost();
        int port = mConnectionConfig.getPort();

        HostnameVerifier hostnameVerifier = null;
        if (!mConnectionConfig.getVerifyCertificate() || mConnectionConfig.isTorHostAddress()) {
            // On Tor we do not need it, as tor already makes sure we are connected with the correct host.
            hostnameVerifier = new BlindHostnameVerifier();
        }

        // Channels are expensive to create. We want to create it once and then reuse it on all our requests.
        if (mConnectionConfig.getUseTor()) {
            mSecureChannel = OkHttpChannelBuilder
                    .forAddress(host, port)
                    .proxyDetector(new TorProxyDetector(TorManager.getInstance().getProxyPort()))//
                    .hostnameVerifier(hostnameVerifier) // null = default hostnameVerifier
                    .sslSocketFactory(LndSSLSocketFactory.create(mConnectionConfig)) // null = default SSLSocketFactory
                    .build();
        } else {
            mSecureChannel = OkHttpChannelBuilder
                    .forAddress(host, port)
                    .hostnameVerifier(hostnameVerifier) // null = default hostnameVerifier
                    .sslSocketFactory(LndSSLSocketFactory.create(mConnectionConfig)) // null = default SSLSocketFactory
                    .build();
        }

        mLndAutopilotService = new RemoteLndAutopilotService(mSecureChannel, mMacaroon);
        mLndChainKitService = new RemoteLndChainKitService(mSecureChannel, mMacaroon);
        mLndChainNotifierService = new RemoteLndChainNotifierService(mSecureChannel, mMacaroon);
        mLndInvoicesService = new RemoteLndInvoicesService(mSecureChannel, mMacaroon);
        mLndLightningService = new RemoteLndLightningService(mSecureChannel, mMacaroon);
        mLndPeersService = new RemoteLndPeersService(mSecureChannel, mMacaroon);
        mLndRouterService = new RemoteLndRouterService(mSecureChannel, mMacaroon);
        mLndSignerService = new RemoteLndSignerService(mSecureChannel, mMacaroon);
        mLndStateService = new RemoteLndStateService(mSecureChannel, mMacaroon);
        mLndVersionerService = new RemoteLndVersionerService(mSecureChannel, mMacaroon);
        mLndWalletKitService = new RemoteLndWalletKitService(mSecureChannel, mMacaroon);
        mLndWatchtowerService = new RemoteLndWatchtowerService(mSecureChannel, mMacaroon);
        mLndWatchtowerClientService = new RemoteLndWatchtowerClientService(mSecureChannel, mMacaroon);
        mLndWalletUnlockerService = new RemoteLndWalletUnlockerService(mSecureChannel, mMacaroon);
    }

    public void openConnection() {
        if (!isConnected) {
            isConnected = true;
            BBLog.d(LOG_TAG, "Starting LND connection...(Open Http Channel)");
            readSavedConnectionInfo();
            generateChannelAndStubs();
            Wallet.getInstance().checkIfLndIsUnlockedAndConnect();
        }

    }

    public void closeConnection() {
        if (mSecureChannel != null) {
            BBLog.d(LOG_TAG, "Shutting down LND connection...(Closing Http Channel)");
            shutdownChannel();
        }

        isConnected = false;
    }

    public void reconnect() {
        if (NodeConfigsManager.getInstance().hasAnyConfigs()) {
            closeConnection();
            openConnection();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Will shutdown the channel and cancel all active calls.
     * Waits for shutdown (blocking) and logs result.
     */
    private void shutdownChannel() {
        try {
            if (mSecureChannel.shutdownNow().awaitTermination(1, TimeUnit.SECONDS)) {
                BBLog.d(LOG_TAG, "LND channel shutdown successfully...");
                Wallet.getInstance().setLNDAsDisconnected();
            } else {
                BBLog.e(LOG_TAG, "LND channel shutdown failed...");
            }
        } catch (InterruptedException e) {
            BBLog.e(LOG_TAG, "LND channel shutdown exception: " + e.getMessage());
        }
    }

    public BBNodeConfig getConnectionConfig() {
        return mConnectionConfig;
    }

}
