package app.michaelwuensch.bitbanana.transactionHistory.listItems;

import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import app.michaelwuensch.bitbanana.R;

public class DateLineViewHolder extends HistoryItemViewHolder {
    private TextView mTvDate;

    public DateLineViewHolder(View v) {
        super(v);
        mTvDate = v.findViewById(R.id.date);
        mContext = v.getContext();
    }

    public void bindDateItem(DateItem dateItem) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, mContext.getResources().getConfiguration().locale);
        String formattedDate = df.format(new Date(dateItem.mDate * 1000L));

        // Check if this date was today or yesterday
        String formattedDateYesterday = df.format(getYesterday());
        String formattedDateToday = df.format(getToday());

        if (formattedDate.equals(formattedDateToday)) {
            mTvDate.setText(mContext.getResources().getString(R.string.today));
        } else {
            if (formattedDate.equals(formattedDateYesterday)) {
                mTvDate.setText(mContext.getResources().getString(R.string.yesterday));
            } else {
                mTvDate.setText(formattedDate);
            }
        }
    }

    private Date getYesterday() {
        return new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
    }

    private Date getToday() {
        return new Date();
    }
}
