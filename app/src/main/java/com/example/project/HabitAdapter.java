package com.example.project;


import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * A class that controls the habits that are displayed in the list as it scrolls
 */
class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitView> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "HabitAdapter";
    private Context mContext;
    private ArrayList<Habit> habitList;

    public HabitAdapter(Context context, ArrayList<Habit> habits) {
        mContext = context;
        habitList = habits;
    }

    @NonNull
    @Override
    public HabitView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_habit, parent, false);
        Log.d(TAG, "New viewholder created");
        return new HabitView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitView holder, final int position) {
        holder.habitName.setText(habitList.get(position).getHabitName());
        holder.target.setText(habitList.get(position).getTarget());
        holder.enddate.setText(habitList.get(position).getEndDate());

        holder.upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                habitList.get(position).incrementProgress();
                notifyItemChanged(position);
                if(habitList.get(position).getProgress() == habitList.get(position).getMaxProgress()){
                    DocumentReference docRef = db.collection("habits").document(habitList.get(position).getHabitId());

                    docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Document has been deleted successfully
                            Toast.makeText(mContext.getApplicationContext(), "Congratulations!! Habit Completed", Toast.LENGTH_LONG).show();
                            habitList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, habitList.size());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // An error occurred while deleting the document
                            Toast.makeText(mContext.getApplicationContext(), "Error deleting document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    docRef.update("Progress", FieldValue.delete())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Progress field has been deleted successfully
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // An error occurred while deleting the progress field
                                }
                            });
                }
            }
        });

        holder.downButton.setOnClickListener(new View.OnClickListener() {
            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext, habitList.get(position).getHabitName(), Toast.LENGTH_SHORT).show();
//            }
            public void onClick(View v) {
                habitList.get(position).decrementProgress();
                notifyItemChanged(position);
            }
        });
        holder.progress.setMax(habitList.get(position).getMaxProgress());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.progress.setProgress(habitList.get(position).getProgress(), true);
        }
//        holder.progress.setProgressTintList(ColorStateList.valueOf(Color.parseColor(r)));
        Log.d(TAG, "Viewholder " + habitList.get(position).getHabitName() + " bound.");
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    class HabitView extends RecyclerView.ViewHolder {

        private TextView habitName;

        private TextView target;
        private TextView enddate;

        private ProgressBar progress;
        private ImageButton upButton;
        private ImageButton downButton;

        public HabitView(@NonNull View itemView) {
            super(itemView);
            habitName = itemView.findViewById(R.id.habitname);
            target = itemView.findViewById(R.id.target);
            enddate = itemView.findViewById(R.id.enddate);

            progress = itemView.findViewById(R.id.progressBar);
            upButton = itemView.findViewById(R.id.habityes);
            downButton = itemView.findViewById(R.id.habitno);
        }
    }
}
