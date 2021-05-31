package com.sarbaevartur.wifistreamscreen.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.elvishew.xlog.XLog
import com.sarbaevartur.wifistreamscreen.R
import com.sarbaevartur.wifistreamscreen.data.model.AppError
import com.sarbaevartur.wifistreamscreen.data.model.FatalError
import com.sarbaevartur.wifistreamscreen.data.model.FixableError
import com.sarbaevartur.wifistreamscreen.data.model.HttpClient
import com.sarbaevartur.wifistreamscreen.data.other.*
import com.sarbaevartur.wifistreamscreen.data.settings.Settings
import com.sarbaevartur.wifistreamscreen.data.settings.SettingsReadOnly
import com.sarbaevartur.wifistreamscreen.service.ServiceMessage
import com.sarbaevartur.wifistreamscreen.service.helper.IntentAction
import com.sarbaevartur.wifistreamscreen.ui.activity.ServiceActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_settings_interface.*
import kotlinx.android.synthetic.main.item_client.*
import org.koin.android.ext.android.inject
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.dialog_settings_resize.*
import kotlinx.android.synthetic.main.dialog_settings_resize.view.*
import kotlinx.android.synthetic.main.fragment_settings_interface.tv_fragment_settings_resize_image_value
import kotlinx.android.synthetic.main.item_device_address.view.*
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Lifecycle
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.sarbaevartur.wifistreamscreen.data.other.getLog

class SettingsInterfaceFragment : Fragment(R.layout.fragment_settings_interface) {

    private val settingsReadOnly: SettingsReadOnly by inject()
    private var httpClientAdapter: SettingsInterfaceFragment.HttpClientAdapter? = null
    private var errorPrevious: AppError? = null
    private val settings: Settings by inject()

    private val colorAccent by lazy { ContextCompat.getColor(requireContext(), R.color.colorAccent) }
    private val clipboard: ClipboardManager? by lazy {
        ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
    }

    private val settingsListener = object : SettingsReadOnly.OnSettingsChangeListener {
        override fun onSettingsChanged(key: String) = when (key) {
            Settings.Key.RESIZE_FACTOR ->
                tv_fragment_settings_resize_image_value.text =
                    getString(R.string.pref_resize_value, settings.resizeFactor)

            Settings.Key.JPEG_QUALITY ->
                tv_fragment_settings_jpeg_quality_value.text = settings.jpegQuality.toString()

            Settings.Key.MAX_FPS ->
                tv_fragment_settings_fps_value.text = settings.maxFPS.toString()

            else -> Unit
        }
    }
    private val screenSize: Point by lazy {
        Point().apply {
            ContextCompat.getSystemService(requireContext(), WindowManager::class.java)
                ?.defaultDisplay?.getRealSize(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Interface - Web page Image buttons
        with(cb_fragment_settings_html_buttons) {
            isChecked = settings.htmlEnableButtons
            setOnClickListener { settings.htmlEnableButtons = isChecked }
            cl_fragment_settings_html_buttons.setOnClickListener { performClick() }
        }

        // Interface - Web page HTML Back color
        v_fragment_settings_html_back_color.color = settings.htmlBackColor
        v_fragment_settings_html_back_color.border = ContextCompat.getColor(requireContext(), R.color.textColorPrimary)
        cl_fragment_settings_html_back_color.setOnClickListener {
            MaterialDialog(requireActivity()).show {
                lifecycleOwner(viewLifecycleOwner)
                title(R.string.pref_html_back_color_title)
                icon(R.drawable.ic_settings_html_back_color_24dp)
                colorChooser(
                    colors = ColorPalette.Primary + Color.parseColor("#000000"),
                    initialSelection = settings.htmlBackColor,
                    allowCustomArgb = true
                ) { _, color -> if (settings.htmlBackColor != color) settings.htmlBackColor = color }
                positiveButton(android.R.string.ok)
                negativeButton(android.R.string.cancel)
            }
        }

        // Image - Resize factor
        tv_fragment_settings_resize_image_value.text = getString(R.string.pref_resize_value, settings.resizeFactor)
        val resizePictureSizeString = getString(R.string.pref_resize_dialog_result)
        cl_fragment_settings_resize_image.setOnClickListener {
            MaterialDialog(requireActivity(), BottomSheet(LayoutMode.WRAP_CONTENT))
                .lifecycleOwner(viewLifecycleOwner)
                .title(R.string.pref_resize)
                .icon(R.drawable.ic_settings_resize_24dp)
                .customView(R.layout.dialog_settings_resize, scrollable = true)
                .positiveButton(android.R.string.ok) { dialog ->
                    dialog.getCustomView().apply DialogView@{
                        val newResizeFactor = tiet_dialog_settings_resize.text.toString().toInt()
                        if (newResizeFactor != settings.resizeFactor) settings.resizeFactor =
                            newResizeFactor
                    }
                }
                .negativeButton(android.R.string.cancel)
                .apply Dialog@{
                    getCustomView().apply DialogView@{
                        tv_dialog_settings_resize_content.text =
                            getString(R.string.pref_resize_dialog_text, screenSize.x, screenSize.y)

                        ti_dialog_settings_resize.isCounterEnabled = true
                        ti_dialog_settings_resize.counterMaxLength = 3

                        with(tiet_dialog_settings_resize) {
                            addTextChangedListener(SettingsInterfaceFragment.SimpleTextWatcher { text ->
                                val isValid =
                                    text.length in 2..3 && text.toString().toInt() in 10..150
                                this@Dialog.setActionButtonEnabled(
                                    WhichButton.POSITIVE, isValid
                                )
                                val newResizeFactor =
                                    (if (isValid) text.toString()
                                        .toInt() else settings.resizeFactor) / 100f
                                this@DialogView.tv_dialog_settings_resize_result.text =
                                    resizePictureSizeString.format(
                                        (screenSize.x * newResizeFactor).toInt(),
                                        (screenSize.y * newResizeFactor).toInt()
                                    )
                            })
                            setText(settings.resizeFactor.toString())
                            setSelection(settings.resizeFactor.toString().length)
                            filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))
                        }

                        tv_dialog_settings_resize_result.text = resizePictureSizeString.format(
                            (screenSize.x * settings.resizeFactor / 100f).toInt(),
                            (screenSize.y * settings.resizeFactor / 100f).toInt()
                        )

                        show()
                    }
                }
        }

        // Image - Jpeg Quality
        tv_fragment_settings_jpeg_quality_value.text = settings.jpegQuality.toString()
        cl_fragment_settings_jpeg_quality.setOnClickListener {
            MaterialDialog(requireActivity(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                lifecycleOwner(viewLifecycleOwner)
                title(R.string.pref_jpeg_quality)
                icon(R.drawable.ic_settings_high_quality_24dp)
                message(R.string.pref_jpeg_quality_dialog)
                input(
                    prefill = settings.jpegQuality.toString(),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    maxLength = 3,
                    waitForPositiveButton = false
                ) { dialog, text ->
                    val isValid = text.length in 2..3 && text.toString().toInt() in 10..100
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                }
                positiveButton(android.R.string.ok) { dialog ->
                    val newValue = dialog.getInputField().text?.toString()?.toInt() ?: settings.jpegQuality
                    if (settings.jpegQuality != newValue) settings.jpegQuality = newValue
                }
                negativeButton(android.R.string.cancel)
                getInputField().filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))
                getInputField().imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
            }
        }

        // Image - Max FPS
        tv_fragment_settings_fps_value.text = settings.maxFPS.toString()
        cl_fragment_settings_fps.setOnClickListener {
            MaterialDialog(requireActivity(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                lifecycleOwner(viewLifecycleOwner)
                title(R.string.pref_fps)
                icon(R.drawable.ic_settings_fps_24dp)
                message(R.string.pref_fps_dialog)
                input(
                    prefill = settings.maxFPS.toString(),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    maxLength = 2,
                    waitForPositiveButton = false
                ) { dialog, text ->
                    val isValid = text.length in 1..2 && text.toString().toInt() in 1..60
                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
                }
                positiveButton(android.R.string.ok) { dialog ->
                    val newValue = dialog.getInputField().text?.toString()?.toInt() ?: settings.maxFPS
                    if (settings.maxFPS != newValue) settings.maxFPS = newValue
                }
                negativeButton(android.R.string.cancel)
                getInputField().filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2))
                getInputField().imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        httpClientAdapter = null
    }


    override fun onStart() {
        super.onStart()
        XLog.d(getLog("onStart", "Invoked"))

        (requireActivity() as ServiceActivity).getServiceMessageLiveData()
            .observe(this, Observer<ServiceMessage> { serviceMessage ->
                when (serviceMessage) {
                    is ServiceMessage.ServiceState -> onServiceStateMessage(serviceMessage)
                }
            })

        IntentAction.GetServiceState.sendToAppService(requireContext())
    }

    private fun onServiceStateMessage(serviceMessage: ServiceMessage.ServiceState) {
        // Interfaces
        ll_fragment_stream_addresses.removeAllViews()
        if (serviceMessage.netInterfaces.isEmpty()) {
            with(layoutInflater.inflate(R.layout.item_device_address, ll_fragment_stream_addresses, false)) {
                tv_item_device_address.setText(R.string.stream_fragment_no_address)
                tv_item_device_address.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorPrimary))
                ll_fragment_stream_addresses.addView(this)
            }
        } else {
            serviceMessage.netInterfaces.sortedBy { it.address.asString() }.forEach { netInterface ->
                with(layoutInflater.inflate(R.layout.item_device_address, ll_fragment_stream_addresses, false)) {
                    val fullAddress = "http://${netInterface.address.asString()}:${settingsReadOnly.severPort}"
                    tv_item_device_address.text = fullAddress.setUnderlineSpan()
                    iv_item_device_address_copy.setOnClickListener {
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText(tv_item_device_address.text, tv_item_device_address.text)
                        )
                        Toast.makeText(
                            requireContext().applicationContext, R.string.stream_fragment_copied, Toast.LENGTH_LONG
                        ).show()
                    }
                    iv_item_device_address_share.setOnClickListener { shareAddress(fullAddress) }
                    iv_item_device_address_qr.setOnClickListener { showQrCode(fullAddress) }
                    ll_fragment_stream_addresses.addView(this)
                }
            }
        }

        // Hide pin on Start
        if (settingsReadOnly.enablePin) {
            val pinText = if (serviceMessage.isStreaming && settingsReadOnly.hidePinOnStart)
                getString(R.string.stream_fragment_pin, "****")
            else
                getString(R.string.stream_fragment_pin, settingsReadOnly.pin)

            tv_fragment_stream_pin.text = pinText.setColorSpan(colorAccent, pinText.length - 4)
        } else {
            tv_fragment_stream_pin.setText(R.string.stream_fragment_pin_disabled)
        }

        showError(serviceMessage.appError)
    }

    private fun shareAddress(fullAddress: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, fullAddress)
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.stream_fragment_share_address)))
    }

    private fun showQrCode(fullAddress: String) {
        fullAddress.getQRBitmap(resources.getDimensionPixelSize(R.dimen.fragment_stream_qrcode_size))?.let { qrBitmap ->
            if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                val imageView = AppCompatImageView(requireContext()).apply { setImageBitmap(qrBitmap) }
                MaterialDialog(requireActivity())
                    .lifecycleOwner(viewLifecycleOwner)
                    .customView(view = imageView, noVerticalPadding = true)
                    .maxWidth(R.dimen.fragment_stream_qrcode_size)
                    .show()
            }
        }
    }

    private fun showError(appError: AppError?) {
        errorPrevious != appError || return

        if (appError == null) {
            tv_fragment_stream_error.visibility = View.GONE
        } else {
            XLog.d(getLog("showError", appError.toString()))
            tv_fragment_stream_error.text = when (appError) {
                is FixableError.AddressInUseException -> getString(R.string.error_port_in_use)
                is FixableError.CastSecurityException -> getString(R.string.error_invalid_media_projection)
                is FixableError.AddressNotFoundException -> getString(R.string.error_ip_address_not_found)
                is FatalError.BitmapFormatException -> getString(R.string.error_wrong_image_format)
                else -> appError.toString()
            }
            tv_fragment_stream_error.visibility = View.VISIBLE
        }

        errorPrevious = appError
    }

    private class HttpClientAdapter : ListAdapter<HttpClient, HttpClientViewHolder>(
        object : DiffUtil.ItemCallback<HttpClient>() {
            override fun areItemsTheSame(oldItem: HttpClient, newItem: HttpClient): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: HttpClient, newItem: HttpClient): Boolean = oldItem == newItem
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            HttpClientViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_client, parent, false))

        override fun getItemId(position: Int): Long = getItem(position).id

        override fun onBindViewHolder(holder: HttpClientViewHolder, position: Int) = holder.bind(getItem(position))
    }

    private class HttpClientViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val textColorPrimary by lazy { ContextCompat.getColor(containerView.context, R.color.textColorPrimary) }
        private val colorError by lazy { ContextCompat.getColor(containerView.context, R.color.colorError) }
        private val colorAccent by lazy { ContextCompat.getColor(containerView.context, R.color.colorAccent) }

        fun bind(product: HttpClient) = with(product) {
            tv_client_item_address.text = clientAddress
            with(tv_client_item_status) {
                when {
                    isDisconnected -> {
                        setText(R.string.stream_fragment_client_disconnected)
                        setTextColor(textColorPrimary)
                    }
                    isSlowConnection -> {
                        setText(R.string.stream_fragment_client_slow_network)
                        setTextColor(colorError)
                    }
                    else -> {
                        setText(R.string.stream_fragment_client_connected)
                        setTextColor(colorAccent)
                    }
                }
            }
        }
    }

    override fun onStop() {
        XLog.d(getLog("onStop", "Invoked"))
        super.onStop()
    }

    private class SimpleTextWatcher(private val afterTextChangedBlock: (s: Editable) -> Unit) : TextWatcher {
        override fun afterTextChanged(s: Editable?) = s?.let { afterTextChangedBlock(it) } as Unit
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    }
}