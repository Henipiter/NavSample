package com.example.navsample


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.navsample.databinding.FragmentShopListBinding


class ShopListFragment : Fragment() {

    private var _binding: FragmentShopListBinding? = null

    private val binding get() = _binding!!

    private lateinit var customAdapter: CustomAdapter
    val args: ShopListFragmentArgs by navArgs()
    var isImageFitToScreen = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.receiptImageBig.visibility = View.INVISIBLE

        binding.receiptImage.setOnClickListener {
            binding.receiptImageBig.visibility = View.VISIBLE
            binding.receiptImage.visibility = View.INVISIBLE
            binding.cameraButton.visibility = View.INVISIBLE
            binding.storageButton.visibility = View.INVISIBLE
            binding.manualButton.visibility = View.INVISIBLE


        }
        binding.receiptImageBig.setOnClickListener {
            binding.receiptImageBig.visibility = View.INVISIBLE
            binding.receiptImage.visibility = View.VISIBLE
            binding.cameraButton.visibility = View.VISIBLE
            binding.storageButton.visibility = View.VISIBLE
            binding.manualButton.visibility = View.VISIBLE
        }


        binding.cameraButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_shopListFragment_to_cameraFragment)
        }


        val pickPhoto = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            binding.receiptImage.setImageURI(it)
            binding.receiptImageBig.setImageURI(it)

        }
        binding.storageButton.setOnClickListener {
                pickPhoto.launch("image/*")



        }

        val bitmap = args.bitmap
        binding.receiptImage.setImageBitmap(bitmap)

    }




}