package com.mylocumchoice.MyLocumChoicePharmacy.view.profile.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mylocumchoice.MyLocumChoicePharmacy.R;
import com.mylocumchoice.MyLocumChoicePharmacy.model.profile.ComplianceDetailsRes;
import com.mylocumchoice.MyLocumChoicePharmacy.model.profile.PrefernceRes;
import com.mylocumchoice.MyLocumChoicePharmacy.presenter.profile.UploadDocVP;
import com.mylocumchoice.MyLocumChoicePharmacy.utils.OnIntentReceived;
import com.mylocumchoice.MyLocumChoicePharmacy.utils.RecyclerViewClickPositionListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;

public class ComplianceDetSectionAdapter extends RecyclerView.Adapter<ComplianceDetSectionAdapter.ViewHolder>
        implements UploadDocVP.View {
    private Context context;
    private RecyclerView.ViewHolder viewHolder;
    private int groupSize = 0;
    private ArrayList<ComplianceDetailsRes.Group> arrayList = new ArrayList<>();
    private String from;
    private Activity mActivity;
    private OnIntentReceived listener;
    private RecyclerViewClickPositionListener itemListener;

    private RecyclerViewClickPositionListener mItemListener;
    private ComplianceDetGroupAdapter adapter;
    private  Response<ComplianceDetailsRes> response;


    public ComplianceDetSectionAdapter(Activity mActivity,
                                       Context context,
                                       Response<ComplianceDetailsRes> response) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.response = response;
        this.mActivity = mActivity;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // ImageView photo;

        /**
         * ButterKnife Code
         **/
        @BindView(R.id.ll_title)
        LinearLayout llTitle;
        @BindView(R.id.shadow)
        View shadow;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.shadow21)
        View shadow21;
        @BindView(R.id.recyclerView)
        android.support.v7.widget.RecyclerView recyclerView;

        /**
         * ButterKnife Code
         **/

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.rv_prefs_section, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(context));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!response.body().getSections().get(position).getName().equalsIgnoreCase("")) {
            holder.tvTitle.setText(response.body().getSections().get(position).getName());
        } else {
            holder.llTitle.setVisibility(View.GONE);
        }
        arrayList = response.body().getSections().get(position).getGroups();
        holder.tvTitle.setText(response.body().getSections().get(position).getName());


        if(arrayList!=null && arrayList.size()>0) {
            adapter = new ComplianceDetGroupAdapter(mActivity, from, context, response, arrayList);
            holder.recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public int getItemCount() {
        return response.body().getSections().size();
    }


    @Override
    public void onUploadResponse(Context context, Response<Void> response) {
        if (response.code() == 204) {
            try {
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUploadComplianceResponse(Context context, Response<Void> response) {

    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showWarning(Activity activity, String title, String msg, String error) {

    }
}