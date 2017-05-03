package ousoftoa.com.xmpp.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;

import java.util.List;

import ousoftoa.com.xmpp.R;


/**
 * Created by 韩莫熙 on 2017/4/24.
 */

public class ChoiceMapAdapter extends BaseQuickAdapter<Geo2AddressResultObject.ReverseAddressResult.Poi,BaseViewHolder>{
    private int mSelectedPosi = 0;

    public ChoiceMapAdapter(List<Geo2AddressResultObject.ReverseAddressResult.Poi> data) {
        super( R.layout.item_location_poi, data );
    }

    @Override
    protected void convert(BaseViewHolder helper, Geo2AddressResultObject.ReverseAddressResult.Poi item) {
        helper.setText(R.id.tvTitle, item.title).setText(R.id.tvDesc, item.address)
                .setVisible(R.id.ivSelected, mSelectedPosi == helper.getLayoutPosition() ? true : false);
    }

    public void setSelectedPosi(int selectedPosi){
        this.mSelectedPosi = selectedPosi;
        this.notifyDataSetChanged();
    }
}
