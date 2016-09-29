package kiwi.jordancrawford.kiwiexplorer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jordan on 29/09/16.
 */
public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityViewHolder> {
    private ArrayList<City> cities;
    private Context context;

    public CityListAdapter(Context context, ArrayList<City> cities) {
        this.context = context;
        this.cities = cities;
    }

    public class CityViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView cityName;
        private RelativeLayout cityContainer;

        public CityViewHolder(View view) {
            super(view);
            this.cityName = (TextView) view.findViewById(R.id.city_list_view_name);
            this.cityContainer = (RelativeLayout) view.findViewById(R.id.city_list_view_container);
        }

        public void setupView(final City city) {
            cityName.setText(city.getName());
            cityContainer.setBackgroundResource(context.getResources().getIdentifier(city.getPictureResourceName(), "drawable", context.getPackageName()));

            cityContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println(city);
                }
            });
        }
    }

    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create the view to display the city.
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_city_view, parent, false);
        return new CityViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(CityViewHolder holder, int position) {
        // Get the item for this index.
        City currentCity = cities.get(position);

        // Setup the view holder with the data.
        holder.setupView(currentCity);
    }

    /**
     * Return the length of the dataset provided.
     * @return
     */
    @Override
    public int getItemCount() {
        return cities.size();
    }
}
