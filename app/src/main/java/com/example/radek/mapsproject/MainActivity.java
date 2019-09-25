package com.example.radek.mapsproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RoutesAdapter routesAdapter;
    private List<Route> routesList = new ArrayList<>();
    private TextView noRoutesView;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        noRoutesView = findViewById(R.id.empty_routes_view);
        databaseHelper = new DatabaseHelper(getApplicationContext());
        routesList.addAll(databaseHelper.getAllRoutes());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showRouteDialog(false, null, -1);
            }
        });

        routesAdapter = new RoutesAdapter(routesList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }
        });
        recyclerView.setAdapter(routesAdapter);

        toggleEmptyRoutes();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                Route route = routesList.get(position);
                String content = route.getRoute();
                Intent intent = new Intent(MainActivity.this, RouteMapsActivity.class);
                intent.putExtra("routeName", content);
                MainActivity.this.startActivity(intent);
               // Toast.makeText(MainActivity.this, fetched, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

                showActionDialog(position);

            }
        }));


    }

    private String getArrayFromJson(String routeName) {
        return databaseHelper.getJsonfromDB(routeName);
    }

    private void createNewRoute(String routeName) {

        long id = databaseHelper.insertRoute(routeName, "text", 0, "");
        Route route = databaseHelper.getRoute(id);

        if (route != null) {
            routesList.add(route);
            routesAdapter.notifyDataSetChanged();
            toggleEmptyRoutes();
        }

    }

   private void updateRouteName(String routeName, int position) {

        Route route = routesList.get(position);
        route.setRoute(routeName);
        databaseHelper.updateRoute(route);
        routesList.set(position, route);
        routesAdapter.notifyDataSetChanged();
        toggleEmptyRoutes();

   }

   private void deleteRoute(int position) {

        databaseHelper.deleteRoute(routesList.get(position));
        routesList.remove(position);
        routesAdapter.notifyDataSetChanged();
        toggleEmptyRoutes();
   }

    private void showActionDialog(final int position) {

        final CharSequence choices [] = new CharSequence[]{"Edycja", "Usuń"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz opcję");
        builder.setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int choiceNumber) {

                if (choiceNumber == 0) {
                    showRouteDialog(true, routesList.get(position), position);
                }
                else {
                    choices[0] = "Tak";
                    choices[1] = "Nie";
                    builder.setTitle("Jesteś pewny?");
                    builder.setItems(choices, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0) {
                                deleteRoute(position);
                            }
                        }
                    });
                    builder.show();
                }
            }
        });
        builder.show();

    }

    private void toggleEmptyRoutes() {

        if(databaseHelper.getRoutesCount() > 0) {
            noRoutesView.setVisibility(View.GONE);
        }
        else {
            noRoutesView.setVisibility(View.VISIBLE);
        }
    }

    private void showRouteDialog(final boolean shouldUpdate, final Route route, final int position) {

        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.note_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);

        final EditText input = view.findViewById(R.id.route);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? "Nowa trasa" : "Edytuj");

        if (shouldUpdate && route != null) {
            input.setText(route.getRoute());
        }

        builder.setCancelable(false).setPositiveButton(shouldUpdate ? "Aktualizuj" : "Zapisz", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(input.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Wprowadź nazwę", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && route != null) {
                    // update note by it's id
                    updateRouteName(input.getText().toString(), position);
                } else {
                    // create new route
                    createNewRoute(input.getText().toString());
                    Intent i = new Intent(MainActivity.this, NewRouteMapsActivity.class);
                    i.putExtra("routeName", input.getText().toString());
                    MainActivity.this.startActivity(i);


                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       /* int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }
}
