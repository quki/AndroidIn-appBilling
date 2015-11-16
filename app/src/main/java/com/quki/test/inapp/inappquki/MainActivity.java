package com.quki.test.inapp.inappquki;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    IInAppBillingService mService;
    Handler mHandler = new Handler();
    Bundle querySkus;
    private TextView textView;
    private TextView parsingData;
    private TextView purchaseResult;
    private static final String VENDING_BILLING = "com.android.vending.billing.InAppBillingService.BIND";
    private static final int SEND_BUY_REQUEST_CODE = 1001;
    String token;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = new Intent(VENDING_BILLING);
        intent.setPackage("com.android.vending");
        bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);


        Button firstButton = (Button) findViewById(R.id.firstid);
        Button secondButton = (Button) findViewById(R.id.secondid);
        textView = (TextView) findViewById(R.id.textView);
        parsingData = (TextView) findViewById(R.id.parsingData);
        purchaseResult = (TextView) findViewById(R.id.purchaseResult);
        imageView = (ImageView) findViewById(R.id.imageView);



        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setTitle("결제").setMessage("정말로 결제하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                //App Billing 서비스에 요청을 하기 위해서는 우선 상품의 아이디 목록을 가지고 있는 Bundle을 생성하여야 합니다.
                                ArrayList<String> skuList = new ArrayList<>();
                                skuList.add("thirdid");  // 상품아이디 등록
                                querySkus = new Bundle();
                                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                                mHandler.post(mRunnable);





                            }
                        }

                ).

                        setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }

                        ).

                        setCancelable(true);

                AlertDialog ad = alertBuilder.create();
                ad.show();


            }
        });
        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setTitle("결제").setMessage("정말로 결제하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                //App Billing 서비스에 요청을 하기 위해서는 우선 상품의 아이디 목록을 가지고 있는 Bundle을 생성하여야 합니다.
                                ArrayList<String> skuList = new ArrayList<>();
                                skuList.add("secondid");  // 상품아이디 등록
                                querySkus = new Bundle();
                                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                                mHandler.post(mSecond);





                            }
                        }

                ).

                        setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }

                        ).

                        setCancelable(true);

                AlertDialog ad = alertBuilder.create();
                ad.show();


            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        /*
             RESPONSE_CODE	        Value is 0 if the purchase was success, error otherwise.
             INAPP_PURCHASE_DATA	A String in JSON format that contains details about the purchase order. See table 4 for a description of the JSON fields.
             INAPP_DATA_SIGNATURE	String containing the signature of the purchase data that was signed with the private key of the developer. The data signature uses the RSASSA-PKCS1-v1_5 scheme.
         */

        if (requestCode == SEND_BUY_REQUEST_CODE) {



            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");


            if (resultCode == RESULT_OK) {


                try {

                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    token = jo.getString("purchaseToken");
                    Toast.makeText(getApplicationContext(), "You have bought the You have bought the" + sku, Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    Log.e("ResultParsingError", e.getMessage());
                }


                try {


                    Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                    int response = ownedItems.getInt("RESPONSE_CODE");

                    if (response == 0) {
                        ArrayList<String> ownedSkus =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList<String> purchaseDataList =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        ArrayList<String> signatureList =
                                ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
                        String continuationToken =
                                ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            String purchaseDatas = purchaseDataList.get(i);
                            String signature = signatureList.get(i);
                            String sku = ownedSkus.get(i);
                            Toast.makeText(getApplicationContext(), "구매한 상품의 정보를 이용하여 무언가를 처리하면 됩니다." + signature, Toast.LENGTH_SHORT).show();
                            // 구매한 상품의 정보를 이용하여 무언가를 처리하면 됩니다.
                            // e.g. 유저가 보유한 상품의 리스트를 업데이트
                        }

                        // 만약 continuationToken != null 이라면 getPurchases를 한번더 호출합니다.
                        // INAPP_CONTINUATION_TOKEN 토큰값을 사용하여 이후의 데이터를 받아올 수 있습니다.


                        mHandler.post(mr);
                    }
                    imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.characterharry));
                } catch (RemoteException e) {
                    Log.e("RemoteException", e.getMessage());
                }


            } else if (resultCode == RESULT_CANCELED) {
                // cancel == 0
                Toast.makeText(getApplicationContext(), "Cancel , result Code : " + resultCode, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Fail , result Code : " + resultCode, Toast.LENGTH_SHORT).show();
            }

        }

    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            try {


                //querySkus null 처리해줘야할듯.

                Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);

                int response = skuDetails.getInt("RESPONSE_CODE");

                if (response == 0) {

                    ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");


                    for (int i = 0; i < responseList.size(); i++) {
                        textView.setText("\n" + i + "번째, " + responseList.get(i));

                    }


                    for (String thisResponse : responseList) {

                        try {
                            JSONObject object = new JSONObject(thisResponse);
                            String sku = object.getString("productId");
                            String price = object.getString("price");
                            String type = object.getString("type");
                            String title = object.getString("title");
                            String description = object.getString("description");
                            if (sku.equals("thirdid")) {

                                parsingData.setText("\n\n파싱파싱파싱테스트 : price : " + price + " type : " + type + " title : " + title + " description : " + description);

                            }

                        } catch (JSONException e) {
                            Log.e("==JSONException==", e.getMessage());
                        }
                    }


                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "thirdid", "inapp", "q+1ukjiq+3ukiq+sis");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");


                    try {


                        // 이미 구매 (owned) 한 물품에 대해서는 pendingIntent가 null
                        if(pendingIntent != null){
                            // 요청보냄
                            startIntentSenderForResult(pendingIntent.getIntentSender(), SEND_BUY_REQUEST_CODE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                        }else{

                            Toast.makeText(getApplicationContext(),"이미 구매한 상품입니다.",Toast.LENGTH_SHORT).show();
                        }

                    } catch (IntentSender.SendIntentException e) {

                        Toast.makeText(getApplicationContext(), "SendIntentException : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                } else {

                    Toast.makeText(getApplicationContext(), "구매실패 response : " + response, Toast.LENGTH_SHORT).show();

                }
            } catch (RemoteException e) {
                Log.e("==HADLEOSException==", e.getMessage());
            }


        }
    };

    Runnable mSecond = new Runnable() {
        @Override
        public void run() {

            try {


                //querySkus null 처리해줘야할듯.

                Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);

                int response = skuDetails.getInt("RESPONSE_CODE");

                if (response == 0) {

                    ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");


                    for (int i = 0; i < responseList.size(); i++) {
                        textView.setText("\n" + i + "번째, " + responseList.get(i));

                    }


                    for (String thisResponse : responseList) {

                        try {
                            JSONObject object = new JSONObject(thisResponse);
                            String sku = object.getString("productId");
                            String price = object.getString("price");
                            String type = object.getString("type");
                            String title = object.getString("title");
                            String description = object.getString("description");
                            if (sku.equals("secondid")) {

                                parsingData.setText("\n\n파싱파싱파싱테스트 : price : " + price + " type : " + type + " title : " + title + " description : " + description);

                            }

                        } catch (JSONException e) {
                            Log.e("==JSONException==", e.getMessage());
                        }
                    }


                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "secondid", "inapp", "qukiqukiqukiquki");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");


                    try {


                        // 이미 구매 (owned) 한 물품에 대해서는 pendingIntent가 null
                        if(pendingIntent != null){
                            // 요청보냄
                            startIntentSenderForResult(pendingIntent.getIntentSender(), SEND_BUY_REQUEST_CODE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                        }else{

                            Toast.makeText(getApplicationContext(),"이미 구매한 상품입니다.",Toast.LENGTH_SHORT).show();
                        }

                    } catch (IntentSender.SendIntentException e) {

                        Toast.makeText(getApplicationContext(), "SendIntentException : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                } else {

                    Toast.makeText(getApplicationContext(), "구매실패 response : " + response, Toast.LENGTH_SHORT).show();

                }
            } catch (RemoteException e) {
                Log.e("==HADLEOSException==", e.getMessage());
            }


        }
    };

    Runnable mr = new Runnable() {
        @Override
        public void run() {
            try{
                int isConsumed = mService.consumePurchase(3, getPackageName(), token);
                Toast.makeText(getApplicationContext(),"isConsumed : "+isConsumed ,Toast.LENGTH_SHORT).show();
                purchaseResult.setText("결재 성공, isConsumed"+isConsumed);
            }catch (RemoteException e){
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    };
}
