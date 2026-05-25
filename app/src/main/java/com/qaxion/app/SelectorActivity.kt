package com.qaxion.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.qaxion.app.databinding.ActivitySelectorBinding

data class Server(val name: String, val key: String, val color: Int)
data class Obra(val code: String, val label: String, val url: String, val server: String)

class SelectorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectorBinding

    private val servers = listOf(
        Server("ZAIT", "zait", R.color.color_zait),
        Server("INOVA TS", "inova", R.color.color_inova)
    )

    // Obras pre-definidas - facilmente expansível
    private val obras = listOf(
        Obra("Z.24.02", "Obra Z.24.02", "https://zait.qaxion.com.br/Z.24.02/index.php", "zait"),
        Obra("Z.24.03", "Obra Z.24.03", "https://zait.qaxion.com.br/Z.24.03/index.php", "zait"),
        Obra("Z.25.03", "Obra Z.25.03", "https://zait.qaxion.com.br/Z.25.03/index.php", "zait"),
        Obra("Z.25.04", "Obra Z.25.04", "https://zait.qaxion.com.br/Z.25.04/index.php", "zait"),
        Obra("Z.25.05", "Obra Z.25.05", "https://zait.qaxion.com.br/Z.25.05/index.php", "zait"),
    )

    private var selectedServer: String = "zait"
    private lateinit var obraAdapter: ObraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupServerChips()
        setupObraList()
        setupCustomUrl()
        updateObraList()
    }

    private fun setupServerChips() {
        servers.forEach { server ->
            val chip = Chip(this).apply {
                text = server.name
                tag = server.key
                isCheckable = true
                isChecked = server.key == selectedServer
                setChipBackgroundColorResource(if (server.key == selectedServer) R.color.primary else R.color.chip_default)
                setTextColor(if (server.key == selectedServer)
                    getColor(R.color.white)
                else
                    getColor(R.color.text_secondary))
            }
            chip.setOnClickListener {
                selectedServer = server.key
                updateChipStates()
                updateObraList()
            }
            binding.chipGroupServers.addView(chip)
        }
    }

    private fun updateChipStates() {
        for (i in 0 until binding.chipGroupServers.childCount) {
            val chip = binding.chipGroupServers.getChildAt(i) as? Chip ?: continue
            val isSelected = chip.tag == selectedServer
            chip.isChecked = isSelected
            chip.setChipBackgroundColorResource(if (isSelected) R.color.primary else R.color.chip_default)
            chip.setTextColor(if (isSelected) getColor(R.color.white) else getColor(R.color.text_secondary))
        }
    }

    private fun setupObraList() {
        obraAdapter = ObraAdapter { obra ->
            openObra(obra.url, obra.label)
        }
        binding.rvObras.apply {
            layoutManager = GridLayoutManager(this@SelectorActivity, 2)
            adapter = obraAdapter
        }
    }

    private fun updateObraList() {
        val filtered = obras.filter { it.server == selectedServer }
        obraAdapter.submitList(filtered)
        binding.tvEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupCustomUrl() {
        binding.btnCustomUrl.setOnClickListener {
            showCustomUrlDialog()
        }
    }

    private fun showCustomUrlDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_custom_url, null)
        val input = view.findViewById<TextInputEditText>(R.id.etCustomUrl)

        MaterialAlertDialogBuilder(this, R.style.QAxionDialog)
            .setTitle("Acessar Obra Personalizada")
            .setView(view)
            .setPositiveButton("Acessar") { _, _ ->
                val url = input.text?.toString()?.trim() ?: ""
                if (url.isNotEmpty()) {
                    val finalUrl = if (url.startsWith("http")) url else "https://$url"
                    openObra(finalUrl, "Obra Personalizada")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openObra(url: String, label: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("url", url)
            putExtra("label", label)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}

// ─── Adapter ──────────────────────────────────────────────
class ObraAdapter(
    private val onClick: (Obra) -> Unit
) : RecyclerView.Adapter<ObraAdapter.ViewHolder>() {

    private var items: List<Obra> = emptyList()

    fun submitList(list: List<Obra>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvObraCode)
        val tvLabel: TextView = view.findViewById(R.id.tvObraLabel)
        val btnAccess: MaterialButton = view.findViewById(R.id.btnAccess)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_obra, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obra = items[position]
        holder.tvCode.text = obra.code
        holder.tvLabel.text = obra.label
        holder.btnAccess.setOnClickListener { onClick(obra) }
        holder.itemView.setOnClickListener { onClick(obra) }
    }

    override fun getItemCount() = items.size
}
