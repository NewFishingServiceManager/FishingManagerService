package com.fishing.namtran.fishingmanagerservice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fishing.namtran.fishingmanagerservice.dbconnection.CustomerManager;
import com.fishing.namtran.fishingmanagerservice.dbconnection.Customers;
import com.fishing.namtran.fishingmanagerservice.dbconnection.FishingManager;
import com.fishing.namtran.fishingmanagerservice.dbconnection.KeepFishing;
import com.fishing.namtran.fishingmanagerservice.dbconnection.KeepFishingManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

/**
 * A login screen that offers login via email/password.
 */
public class AddNewCustomerActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private CustomerActionTask mCustomerTask = null;

    // UI references.
    private AutoCompleteTextView mFullNameView;
    private EditText mIdNumberView;
    private EditText mDateInView;
    private TextInputLayout mTextInputLayoutView;
    private EditText mNoteView;
    private CheckBox mFeedTypeView;
    private View mProgressView;
    private View mSubmitFormView;
    private Cursor SearchCustomers;
    private static final int afterTime = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_customer);

        // Set up the login form.
        mIdNumberView = (EditText) findViewById(R.id.id_number);
        mFullNameView = (AutoCompleteTextView) findViewById(R.id.fullname);
        mNoteView = (EditText) findViewById(R.id.note);
        mFeedTypeView = (CheckBox) findViewById(R.id.feed_type);
        mSubmitFormView = findViewById(R.id.add_new_customer_form);
        mProgressView = findViewById(R.id.add_new_customer_progress);

        Button mAddNewCustomerButton = (Button) findViewById(R.id.add_new_customer_button);
        mAddNewCustomerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSubmit();
            }
        });

        if(Utils.checkAfterTime(afterTime)) {
            Utils util = new Utils();
            util.CreateEditText(this, "", R.id.date_in, getString(R.string.date_in), InputType.TYPE_CLASS_TEXT, 1, 3);

            mDateInView = (EditText) findViewById(R.id.date_in);

            //Focus date in
            mDateInView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        EditText editText = (EditText) mDateInView;
                        GetTimePicker(editText);
                    }
                }
            });
        }

        //Get customers from database by current day
        DateFormat currentDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date currentDate = new Date();

        CustomerManager customerManager = new CustomerManager(getApplicationContext());
        //SearchCustomers = customerManager.getSearchCustomers(currentDateFormat.format(currentDate));
        SearchCustomers = customerManager.getSearchAllCustomers();
        searchCustomers(SearchCustomers);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //searchCustomers(SearchCustomers);
    }

    private void searchCustomers(Cursor searchCustomers)
    {
        //Search text
        final ListView itemList = (ListView)findViewById(R.id.listView);
        String [] listViewAdapterContent; //{"School", "House", "Building", "Food", "Sports", "Dress", "Ring", "School", "House", "Building", "Food", "Sports", "Dress", "Ring", "School", "House", "Building", "Food", "Sports", "Dress", "Ring"};
        final ArrayAdapter<String> listAdapter;

        int i = 0;
        listViewAdapterContent = new String[searchCustomers.getCount()];
        while (searchCustomers.moveToNext())
        {
            listViewAdapterContent[i] = searchCustomers.getString(searchCustomers.getColumnIndexOrThrow(Customers.Properties.FULLNAME)) + " - " + searchCustomers.getString(searchCustomers.getColumnIndexOrThrow(Customers.Properties.ID_NUMBER));
            i++;
        }

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listViewAdapterContent);
        itemList.setTextFilterEnabled(true);
        itemList.setAdapter(listAdapter);
        itemList.setVisibility(View.GONE);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // make Toast when click
                //Toast.makeText(getApplicationContext(), "Position " + position, Toast.LENGTH_LONG).show();
                String item = listAdapter.getItem(position);
                String[] fullname = item.split("-");
                mFullNameView.setText(fullname[0].trim());
                mIdNumberView.setText(fullname[1].trim());
                itemList.setVisibility(View.GONE);
            }
        });

        mFullNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                itemList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mFullNameView.getText().toString().equals("") || listAdapter.isEmpty()) {
                    itemList.setVisibility(View.GONE);
                    mIdNumberView.setText("");
                }
            }
        });
    }

    public void GetTimePicker(final Object objText)
    {
        //https://www.journaldev.com/9976/android-date-time-picker-dialog
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        EditText editText = (EditText) objText;
                        editText.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSubmit() {
        if (mCustomerTask != null) {
            return;
        }

        // Reset errors.
        mFullNameView.setError(null);
        mFeedTypeView.setError(null);
        mFeedTypeView.setError(null);

        if(Utils.checkAfterTime(afterTime))
        {
            mDateInView.setError(null);
        }

        // Store values at the time of the login attempt.
        String fullname = mFullNameView.getText().toString();
        String mobile = mIdNumberView.getText().toString();
        String dateIn = Utils.checkAfterTime(afterTime) ? mDateInView.getText().toString() : null;
        String note = mNoteView.getText().toString();
        boolean feedType = mFeedTypeView.isChecked();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(fullname)) {
            mFullNameView.setError(getString(R.string.error_field_required));
            focusView = mFullNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(dateIn) && Utils.checkAfterTime(afterTime)) {
            mDateInView.setError(getString(R.string.error_field_required));
            focusView = mDateInView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mCustomerTask = new CustomerActionTask(fullname, mobile, dateIn, feedType, note);
            mCustomerTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSubmitFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSubmitFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSubmitFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mSubmitFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous action task
     */
    public class CustomerActionTask extends AsyncTask<Void, Void, Boolean> {

        private final String mFullName;
        private final String mIdNumber;
        private final String mDateIn;
        private final String mNote;
        private final boolean mFeedType;

        CustomerActionTask(String fullname, String idNumber, String dateIn, boolean feedType, String note) {
            mFullName = fullname;
            mIdNumber = idNumber;
            mDateIn = dateIn;
            mNote = note;
            mFeedType = feedType;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCustomerTask = null;
            showProgress(false);
            DateFormat currentDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date currentDate = new Date();
            String fullDateIn = Utils.checkAfterTime(afterTime) ? currentDateFormat.format(currentDate) + " " + mDateIn + ":00" : (new SimpleDateFormat("yyyy/MM/dd HH:mm:00")).format(Utils.GetCurrentTimeByRoundFiveMinutes());

            if (success) {
                CustomerManager customer = new CustomerManager(getApplicationContext());
                FishingManager fishing = new FishingManager(getApplicationContext());
                KeepFishingManager keepFishing = new KeepFishingManager(getApplicationContext());

                long custId = customer.createCustomer(mFullName, mIdNumber);
                long fishingId = fishing.createFishingEntry(custId, fullDateIn, mFeedType ? 1 : 0, mNote);
                keepFishing.createKeepFishingEntry(custId, 0, 0, 0, 0, 0, "");
                finish();
            } else {
                Utils.Alert(AddNewCustomerActivity.this, getString(R.string.action_error));
            }
        }

        @Override
        protected void onCancelled() {
            mCustomerTask = null;
            showProgress(false);
        }
    }
}

