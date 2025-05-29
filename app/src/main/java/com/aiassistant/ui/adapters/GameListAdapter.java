package com.aiassistant.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.data.models.Game;

import java.util.List;

/**
 * Adapter for displaying the list of games in a RecyclerView
 */
public class GameListAdapter extends RecyclerView.Adapter<GameListAdapter.GameViewHolder> {
    
    private List<Game> games;
    private OnGameSelectedListener listener;
    
    /**
     * Interface for game selection callback
     */
    public interface OnGameSelectedListener {
        void onGameSelected(Game game);
    }
    
    /**
     * Constructor
     * 
     * @param games The games
     * @param listener The listener
     */
    public GameListAdapter(List<Game> games, OnGameSelectedListener listener) {
        this.games = games;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bind(game);
    }
    
    @Override
    public int getItemCount() {
        return games != null ? games.size() : 0;
    }
    
    /**
     * Update the games list
     * 
     * @param games The games
     */
    public void updateGames(List<Game> games) {
        this.games = games;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for game items
     */
    class GameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView nameTextView;
        private final TextView typeTextView;
        private final ImageView iconImageView;
        private Game game;
        
        /**
         * Constructor
         * 
         * @param itemView The item view
         */
        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textGameName);
            typeTextView = itemView.findViewById(R.id.textGameType);
            iconImageView = itemView.findViewById(R.id.imageGameIcon);
            itemView.setOnClickListener(this);
        }
        
        /**
         * Bind a game to the view
         * 
         * @param game The game
         */
        public void bind(Game game) {
            this.game = game;
            nameTextView.setText(game.getName());
            typeTextView.setText(game.getGameType());
            
            // Load icon if available
            byte[] iconData = game.getIconData();
            if (iconData != null) {
                // In a real implementation, this would use BitmapFactory
                // For this implementation, we'll just hide the icon
                iconImageView.setVisibility(View.GONE);
            } else {
                iconImageView.setVisibility(View.GONE);
            }
        }
        
        @Override
        public void onClick(View v) {
            if (listener != null && game != null) {
                listener.onGameSelected(game);
            }
        }
    }
}
