package app.michaelwuensch.bitbanana.transactionHistory.listItems;

import com.google.protobuf.ByteString;

public abstract class TransactionItem extends HistoryListItem {

    abstract public ByteString getTransactionByteString();
}
