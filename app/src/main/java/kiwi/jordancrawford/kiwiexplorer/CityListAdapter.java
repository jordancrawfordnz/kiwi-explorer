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
 * An adapter for the city list in the main activity.
 *
 * Created by Jordan on 29/09/16.
 */
public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityViewHolder> {
    public static final String CITY_CLICK_KEY = "city_click";
    public static final String CITY_SEEN_CLICK_KEY = "city_seen_click";
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
        private ImageView cityPicture, citySeenIcon;

        /**
         * Setup objects for the components of the view.
         * @param view
         */
        public CityViewHolder(View view) {
            super(view);
            this.view = view;
            this.cityName = (TextView) view.findViewById(R.id.city_list_view_name);
            this.currentLocationText = (TextView) view.findViewById(R.id.city_list_current_location);
            this.cityPicture = (ImageView)view.findViewById(R.id.city_list_view_image);
            this.citySeenIcon = (ImageView)view.findViewById(R.id.city_list_have_seen);
        }

        /**
         * Fill in the views for a city.
         * @param city
         */
        public void setupView(final City city) {
            cityName.setText(city.getName());
            cityPicture.setImageResource(context.getResources().getIdentifier(city.getPictureResourceName(), "drawable", context.getPackageName()));

            // Fill in the current location text if appropriate.
            if (city.getCityData() != null && city.getCityData().isCurrentLocation()) {
                currentLocationText.setText(R.string.current_location_text);
            } else {
                currentLocationText.setText("");
            }

            // Change the citySeenIcon based on whether a city has been seen or not.
            if (city.getCityData() != null && city.getCityData().isCitySeen()) {
                citySeenIcon.setImageResource(R.drawable.ic_checkbox_marked_circle_white_48dp);
            } else {
                citySeenIcon.setImageResource(R.drawable.ic_checkbox_blank_circle_outline_white_48dp);
            }

            // Send a broadcast when the city seen icon is clicked.
            citySeenIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CITY_SEEN_CLICK_KEY);
                    intent.putExtra(CITY_EXTRA, city);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            });

            // Send a broadcast when the city is clicked.
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

    /**
     * Setup a view holder.
     *
     * @param parent
     * @param viewType
     * @return
     */
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create the view to display the city.
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_city_view, parent, false);
        return new CityViewHolder(inflatedView);
    }

    /**
     * Setup a view holder with a city.
     * @param holder
     * @param position
     */
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
