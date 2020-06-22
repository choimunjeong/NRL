package Page3_1_1_1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nrr.hansol.spot_200510_hs.R;

import java.util.ArrayList;
import java.util.List;

public class Page3_1_1_1_FirstItemAdapter extends RecyclerView.Adapter<Page3_1_1_1_FirstItemAdapter.ViewHolder> {
    Page3_1_1_1_SecondAdapter adapter;
    private List<Page3_1_1_1_Main.RecycleItem> items;

    private List<Page3_1_1_1_Main.RecycleItem> listForSecond = new ArrayList<>();    // 두 번째 어댑터로 보낼 어레이
    Page3_1_1_1_addCityBottomSheet.onSetList listener;
    private Page3_1_1_1_SecondAdapter.OnDismiss onDismiss;

    // 어댑터에 들어가 리스트
    private ArrayList<String> listData;
    private Context context;

    // 아이템 클릭상태 저장
    private SparseBooleanArray selectedItems = new SparseBooleanArray();

    private AdapterView.OnItemClickListener onClickListener;

    public Page3_1_1_1_FirstItemAdapter(ArrayList<String> listData, List<Page3_1_1_1_Main.RecycleItem> items, Page3_1_1_1_addCityBottomSheet.onSetList listener, Page3_1_1_1_SecondAdapter.OnDismiss onDismiss) {
        this.listData = listData;
        this.items = items;
        this.listener = listener;
        this.onDismiss = onDismiss;
    }

    // 직전에 클릭됐던 item의 position
    private int prePosition = -1;

    @NonNull
    @Override
    public Page3_1_1_1_FirstItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.page3_1_1_1_addcity_first_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Page3_1_1_1_FirstItemAdapter.ViewHolder holder, final int position) {
        Page3_1_1_1_Main.RecycleItem item = items.get(position);

        listForSecond.clear();

        for (int i = 0 ; i < items.size() ; i++) {
            if (listData.get(position).equals(items.get(i).cityName)) {
                listForSecond.add(items.get(i));
            }
        }

        //리사이클러뷰 넣는 부분
        holder.recyclerView.setLayoutManager( new LinearLayoutManager(context));
        adapter = new Page3_1_1_1_SecondAdapter(listData, listForSecond, listener, onDismiss);
        holder.recyclerView.setAdapter(adapter);

        holder.onBind(position);

        holder.city.setText(listData.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItems.get(position)) {
                    selectedItems.delete(position);
                } else {
                    selectedItems.delete(prePosition);
                    selectedItems.put(position, true);
                }
                if (prePosition != -1) notifyItemChanged(prePosition);
                notifyItemChanged(position);
                prePosition = position;
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView city;
        private RecyclerView recyclerView;
        private int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            city = itemView.findViewById(R.id.page3_1_1_1_city);
            recyclerView = itemView.findViewById(R.id.page3_1_1_1_rv);
        }

        void onBind(int position) {
            this.position = position;
            changeVisibility(selectedItems.get(position));
        }

        private void changeVisibility(final boolean isExpanded) {
            int dpValue = 300;
            float d = context.getResources().getDisplayMetrics().density;
            int height = (int) (dpValue * d);

            ValueAnimator va = isExpanded ? ValueAnimator.ofInt(0, height) : ValueAnimator.ofInt(height, 0);
            va.setDuration(600);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    recyclerView.getLayoutParams().height = value;
                    recyclerView.requestLayout();
                    recyclerView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                }
            });
            va.start();
        }
    }
}
