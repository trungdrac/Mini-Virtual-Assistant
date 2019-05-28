package com.example.minivirtualassistant;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class GetPhoneNumberFromContact {
    public String getPhoneNumber(String name, Context context) {
        String phoneNumber = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'" + name +"'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, null);
        if (cursor.moveToFirst()) {
            phoneNumber = cursor.getString(0);
        }
        cursor.close();
        if(phoneNumber==null)
            phoneNumber = "unsaved";
        return phoneNumber;
    }
}
