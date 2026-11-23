package com.safedriveai.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val sessions: List<MonitoringSession>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val dateLabel: TextView =
            itemView.findViewById(R.id.dateLabel)

        val durationLabel: TextView =
            itemView.findViewById(R.id.durationLabel)

        val drowsyLabel: TextView =
            itemView.findViewById(R.id.drowsyLabel)

        val yawnLabel: TextView =
            itemView.findViewById(R.id.yawnLabel)

        val alarmLabel: TextView =
            itemView.findViewById(R.id.alarmLabel)

        val riskLabel: TextView =
            itemView.findViewById(R.id.riskLabel)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.history_item,
                    parent,
                    false
                )

        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int
    ) {

        val session = sessions[position]

        val dateFormat =
            SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            )

        holder.dateLabel.text =
            dateFormat.format(
                Date(session.date)
            )

        val totalSeconds =
            session.duration / 1000

        val minutes =
            totalSeconds / 60

        val seconds =
            totalSeconds % 60

        holder.durationLabel.text =
            "Duration: %02d:%02d".format(
                minutes,
                seconds
            )

        holder.drowsyLabel.text =
            "Drowsy Events: ${session.drowsyEvents}"

        holder.yawnLabel.text =
            "Yawns: ${session.yawns}"

        holder.alarmLabel.text =
            "Alarm Activations: ${session.alarms}"

        holder.riskLabel.text =
            "Highest Risk: ${session.highestRisk}"
    }

    override fun getItemCount(): Int {
        return sessions.size
    }
}