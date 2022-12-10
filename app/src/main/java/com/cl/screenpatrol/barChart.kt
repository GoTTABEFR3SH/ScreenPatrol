package com.cl.screenpatrol

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet


class barChart : Fragment() {
    // TODO: Rename and change types of parameters
    var barChart:BarChart = requireActivity().findViewById(R.id.barChart)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bar_chart, container, false)
    }

        fun setBarChatValues(){
            val entries = ArrayList<BarEntry>()
            entries.add(BarEntry(8f, 0f))
            entries.add(BarEntry(2f, 1f))
            entries.add(BarEntry(5f, 2f))
            entries.add(BarEntry(20f, 3f))
            entries.add(BarEntry(15f, 4f))
            entries.add(BarEntry(19f, 5f))

            val barDataSet = BarDataSet(entries, "Cells")

            val labels = ArrayList<String>()
            labels.add("18-Jan")
            labels.add("19-Jan")
            labels.add("20-Jan")
            labels.add("21-Jan")
            labels.add("22-Jan")
            labels.add("23-Jan")

            val bar: ArrayList<IBarDataSet> = ArrayList<IBarDataSet>()
            bar.add((IBarDataSet) labels)
            val data = BarData(labels, barDataSet)
            barChart.data = data // set the data and list of lables into chart



            barChart.animateY(5000)



        }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment barChart.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            barChart().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}