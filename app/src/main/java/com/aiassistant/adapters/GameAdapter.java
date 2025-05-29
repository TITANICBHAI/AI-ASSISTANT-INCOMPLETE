package com.aiassistant.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aiassistant.R;
import com.aiassistant.data.models.Game;
import com.aiassistant.data.models.GameType;

import java.io.File;
import java.util.List;

/**
 * Adapter for displaying games in a RecyclerView
 */
public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {
    
    private List<Game> games;
    private final GameClickListener clickListener;
    
    /**
     * Interface for game click events
     */
    public interface GameClickListener {
        void onGameClicked(Game game);
        void onGameLongClicked(Game game);
    }
    
    public GameAdapter(List<Game> games, GameClickListener clickListener) {
        this.games = games;
        this.clickListener = clickListener;
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
        
        holder.gameNameText.setText(game.getDisplayName());
        
        // Set game type
        GameType gameType = GameType.fromId(game.getGameType());
        holder.gameTypeText.setText(gameType.getDisplayName());
        
        // Load game icon
        if (game.getIconPath() != null && !game.getIconPath().isEmpty()) {
            File iconFile = new File(game.getIconPath());
            if (iconFile.exists()) {
                holder.gameIconImage.setImageURI(Uri.fromFile(iconFile));
            } else {
                // Fallback icon
                holder.gameIconImage.setImageResource(R.drawable.ic_game_default);
            }
        } else {
            // Default icon
            holder.gameIconImage.setImageResource(R.drawable.ic_game_default);
        }
        
        // Set support indicator
        holder.gameSupportedIndicator.setVisibility(game.isSupported() ? View.VISIBLE : View.GONE);
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGameClicked(game);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                clickListener.onGameLongClicked(game);
                return true;
            }
            return false;
        });
    }
    
    @Override
    public int getItemCount() {
        return games.size();
    }
    
    /**
     * Update the game list and refresh the adapter
     * 
     * @param newGames The new list of games
     */
    public void updateGames(List<Game> newGames) {
        this.games = newGames;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for game items
     */
    static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView gameIconImage;
        TextView gameNameText;
        TextView gameTypeText;
        View gameSupportedIndicator;
        
        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameIconImage = itemView.findViewById(R.id.gameIconImage);
            gameNameText = itemView.findViewById(R.id.gameNameText);
            gameTypeText = itemView.findViewById(R.id.gameTypeText);
            gameSupportedIndicator = itemView.findViewById(R.id.gameSupportedIndicator);
        }
    }
}
