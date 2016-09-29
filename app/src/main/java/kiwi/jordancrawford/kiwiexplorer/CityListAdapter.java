package kiwi.jordancrawford.kiwiexplorer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Jordan on 29/09/16.
 */
public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityViewHolder> {
    public static final String CITY_CLICK_KEY = "city_click";
    public static final String CITY_EXTRA = "city_extra";

    private ArrayList<City> cities;
    private Context context;

    public CityListAdapter(Context context, ArrayList<City> cities) {
        this.context = context;
        this.cities = cities;
    }

    public class CityViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView cityName, currentLocationText;
        private ImageView cityPicture;

        public CityViewHolder(View view) {
            super(view);
            this.view = view;
            this.cityName = (TextView) view.findViewById(R.id.city_list_view_name);
            this.currentLocationText = (TextView) view.findViewById(R.id.city_list_current_location);
            this.cityPicture = (ImageView)view.findViewById(R.id.city_list_view_image);
        }

        public void setupView(final City city) {
            cityName.setText(city.getName());
            cityPicture.setImageResource(context.getResources().getIdentifier(city.getPictureResourceName(), "drawable", context.getPackageName()));

            // Fill in the current location text if appropriate.
            if (city.getCityData() != null && city.getCityData().isCurrentLocation()) {
                System.out.println("Is current location");
                System.out.println(city);
                currentLocationText.setText(R.string.current_location_text);
            } else {
                currentLocationText.setText("");
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CITY_CLICK_KEY);
                    intent.putExtra(CITY_EXTRA, city);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
