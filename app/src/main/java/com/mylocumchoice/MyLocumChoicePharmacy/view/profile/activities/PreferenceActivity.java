package com.mylocumchoice.MyLocumChoicePharmacy.view.profile.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mylocumchoice.MyLocumChoicePharmacy.R;
import com.mylocumchoice.MyLocumChoicePharmacy.model.profile.PrefernceRes;
import com.mylocumchoice.MyLocumChoicePharmacy.presenter.profile.PolarFieldVP;
import com.mylocumchoice.MyLocumChoicePharmacy.presenter.profile.PreferenceVP;
import com.mylocumchoice.MyLocumChoicePharmacy.presenter.profile.PreferncePresenter;
import com.mylocumchoice.MyLocumChoicePharmacy.presenter.profile.UploadDocPresenter;
import com.mylocumchoice.MyLocumChoicePharmacy.presenter.profile.UploadDocVP;
import com.mylocumchoice.MyLocumChoicePharmacy.utils.ApiResponseListener;
import com.mylocumchoice.MyLocumChoicePharmacy.utils.RecyclerViewClickPositionListener;
import com.mylocumchoice.MyLocumChoicePharmacy.utils.SignOutAlert;
import com.mylocumchoice.MyLocumChoicePharmacy.utils.Utils;
import com.mylocumchoice.MyLocumChoicePharmacy.view.base.AppActivity;
import com.mylocumchoice.MyLocumChoicePharmacy.view.profile.adapters.PreferenceAdapterFields;
import com.mylocumchoice.MyLocumChoicePharmacy.view.profile.adapters.PreferenceAdapterSections;
import com.mylocumchoice.MyLocumChoicePharmacy.view.shifts.activities.LandingActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import droidninja.filepicker.FilePickerConst;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PreferenceActivity extends AppActivity implements PreferenceVP.View ,
        UploadDocVP.View,
        PolarFieldVP.View,
        PreferenceAdapterFields.CallbackInterface,
        RecyclerViewClickPositionListener,ApiResponseListener{

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_header)
    TextView tv_header;
    @BindView(R.id.ll_back)
    LinearLayout llBack;
    @BindView(R.id.tv_back)
    ImageView tvBack;
    @BindView(R.id.tv_dots)
    ImageView tvDots;
    @BindView(R.id.ll_menuRight)
    FrameLayout llMenuRight;
    @BindView(R.id.llSticky)
    LinearLayout llSticky;
    @BindView(R.id.tv_title)
    TextView tv_title;
    private int overallYScroll = 0;

    private SignOutAlert signOutAlert;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 101;


    String[] from = { "flag","txt","cur" };
    // Ids of views in listview_layout
    ArrayList<Integer> itemsimg = new ArrayList<Integer>();
    private PreferncePresenter presenter;

    public int NOTIFICATION_RESULT_CODE=1001;


    private String filepath = "";
    private String filename = "";
    private String docUrl, docName;
    private boolean isDocument = false;
    private boolean isCamera = false;
    private boolean isPhoto = false;
    private ArrayList<String> docPaths;
    private ArrayList<String> photoPaths;
    private String fileTypes[] = {".rtf",".ppt"};
    private String field_id;


    private LinearLayoutManager linearLayoutManager;
    private int currentPosition = 0;

    private UploadDocPresenter presenterUpload;
    private Utils mUtils;

    private static final int ACTIVITY_REQUEST = 110;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        ButterKnife.bind(this);
        itemsimg.add(R.drawable.rb_on);
        itemsimg.add(R.drawable.rb_off);
        overallYScroll = recyclerView.getScrollY();
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(SelectPharmacyActivity.class);
            }
        });

        tv_header.setText(getResources().getString(R.string.text_pref));
        tvDots.setVisibility(View.INVISIBLE);
        recyclerView.setNestedScrollingEnabled(false);
        getDetails();
    }

    @OnClick(R.id.ll_back)
    public void onBackClick(){
        try {
            if (getIntent().getExtras() != null) {
                if (getIntent().getExtras().get("from").toString().equalsIgnoreCase("profileFrag")) {
                    finish();
                } else if (getIntent().getExtras().get("from").toString().equalsIgnoreCase("notification")) {
                    Intent mIntent = new Intent();
                    setResult(NOTIFICATION_RESULT_CODE, mIntent);
                    finish();
                } else if (getIntent().getExtras().get("from").toString().equalsIgnoreCase("notificationAdapter")) {
                    Intent intent = new Intent(this, LandingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    LandingActivity.openFragmentPosition = 4;
                    overridePendingTransitionExit();
                } else {
                    onBackPressed();
                }

            } else {
                onBackPressed();
            }
            finish();
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }


    public void getDetails(){
        presenter = new PreferncePresenter(this, this);

        try {
            if (getIntent().getExtras().get("from").toString().equalsIgnoreCase("notification")) {
                presenter.getPreferenceNotification(this, getIntent().getIntExtra("notification_id", 0));
            } else if (getIntent().getExtras().get("from").toString().equalsIgnoreCase("notificationAdapter")) {
                presenter.getPreferenceNotification(this, getIntent().getIntExtra("notification_id", 0));
            } else {
                presenter.getPreference(this);
            }
        }catch (Exception e){
            e.printStackTrace();
            presenter.getPreference(this);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                try{
                    if(resultCode== Activity.RESULT_OK && data!=null)
                    {
                        isPhoto = true;
                        isCamera = false;
                        isDocument = false;
                        photoPaths = new ArrayList<>();
                        photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                        filepath = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA).get(0).toString();
                        uploadDoc(filepath);
                    }
                    break;
                }catch (Exception e){
                    e.printStackTrace();
                }


            case FilePickerConst.REQUEST_CODE_DOC:
                try{
                    if(resultCode== Activity.RESULT_OK && data!=null)
                    {
                        isDocument = true;
                        isCamera = false;
                        isPhoto = false;
                        if (resultCode == Activity.RESULT_OK && data != null) {
                            docPaths = new ArrayList<>();
                            docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                            filepath = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS).get(0).toString();
                            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                            File folder = new File(extStorageDirectory, "doc");
                            filename = filepath.substring(filepath.lastIndexOf("/")+1);
                            folder.mkdir();
                            File file = new File(folder, filename);
                            try {
                                file.createNewFile();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            uploadDoc(filepath);
                        }
                    }
                    break;
                }catch (Exception e){
                    e.printStackTrace();
                }

            case CAMERA_REQUEST:
                try{
                    if (resultCode == RESULT_OK) {
                        isCamera = true;
                        isPhoto = false;
                        isDocument = false;
                        filepath = getRealPathFromURI(PreferenceAdapterFields.imageUri);
                        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
                        Log.d("FilePath", "" + filepath);
                        uploadDoc(filepath);
                        break;
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }

            case ACTIVITY_REQUEST:
                try{
                    if (resultCode == RESULT_OK) {
                        getDetails();
                    }
                    break;
                }catch (Exception e){
                    e.printStackTrace();
                }

            case 2:
                try{
                    getDetails();
                    break;
                }catch (Exception e){
                    e.printStackTrace();
                }
        }
    }



    public void uploadDoc(String filepath){
        presenterUpload = new UploadDocPresenter(this, this);
        File file;
        file = new File(filepath);
        long fileSizeInBytes = file.length();
        long fileSizeInKB = fileSizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;

        if (fileSizeInMB < 10) {
            RequestBody rDoc = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            RequestBody field = RequestBody.create(MediaType.parse("text/plain"), field_id);
            MultipartBody.Part mpDoc = MultipartBody.Part.createFormData("response[document]", file.getName(), rDoc);
            presenterUpload.uploadDoc(this, mpDoc,field);
        }else{
            showWarning(this, "", "Document must be less than 10MB", "error");
        }


    }


    @Override
    public void onHandleSelection(int position, Integer id) {
        field_id = String.valueOf(id);
    }



    //API Responses Handled
    @Override
    public void onGetPreference(Response<PrefernceRes> response) {
            if(response.code() == 200){
                PreferenceAdapterSections adapter = new PreferenceAdapterSections(PreferenceActivity.this, "Preferences", this, response, null, this,this);
                recyclerView.setAdapter(adapter);


                /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        currentPosition = linearLayoutManager.findFirstVisibleItemPosition();

                        if(dy > 0){
                            llSticky.setPadding(0,0,0,0);
                        }else{
                            llSticky.setPadding(0,(int)getResources().getDimension(R.dimen._20sdp),0,0);
                        }

                        for(int position = 0;  position < response.body().getSections().size(); position++) {
                            if (currentPosition == position) {
                                if(!response.body().getSections().get(position).getName().equalsIgnoreCase("")) {
                                    llSticky.setVisibility(View.VISIBLE);
                                    tv_title.setText(response.body().getSections().get(position).getName());
                                }else{
                                    llSticky.setVisibility(View.GONE);
                                }
                            }
                        }

                    }
                });*/
            }
            else {
                handleAPIErrors(this, response);
            }
    }


    @Override
    public void onFieldResponse(Context context, Response<Void> response) {
        if (response.code() == 204) {

        }
        else {
            handleAPIErrors(this, response);
        }
    }


    @Override
    public void onUploadResponse(Context context, Response<Void> response) {
        if(response.code() == 204){
            try {
                getDetails();
                recyclerView.scrollTo(0, overallYScroll);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else {
            handleAPIErrors(this, response);
        }
    }

    @Override
    public void onUploadComplianceResponse(Context context, Response<Void> response) {

    }

    @Override
    public void recyclerViewListClicked(View v, int position) {

    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try{

        }catch (Exception e){
            e.printStackTrace();
        }
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
//                Intent cameraIntent = new
//                        Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                showWarning(this, "", "Camera and storage permission denied", "error");
            }

        }

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {

            } else {
                showWarning(this, "", "Storage permission denied", "error");
                mUtils = new Utils();
                mUtils.showPermDialog(this,"", getResources().getString(R.string.providePerm) );
            }
        }
    }



    @Override
    public void onBackPressed() {
        try {
            if (getIntent().getExtras() != null) {
                if (getIntent().getExtras().get("from").toString().equalsIgnoreCase("profileFrag")) {
                    finish();
                } else if (getIntent().getExtras().get("from").toString().equalsIgnoreCase("notification")) {
                    Intent mIntent = new Intent();
                    setResult(NOTIFICATION_RESULT_CODE, mIntent);
                    finish();
                } else if (getIntent().getExtras().get("from").toString().equalsIgnoreCase("notificationAdapter")) {
                    Intent intent = new Intent(this, LandingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    LandingActivity.openFragmentPosition = 4;
                    overridePendingTransitionExit();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onGetResponse(Response<Void> response) {
        handleAPIErrors(this, response);
    }
}
