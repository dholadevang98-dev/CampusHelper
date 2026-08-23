package com.devang.campushelper

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var prefHelper: PreferenceHelper
    private var currentRole: UserRole = UserRole.STUDENT
    private var activeNavTab: Int = 0

    enum class UserRole {
        STUDENT, FACULTY, ADMIN
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefHelper = PreferenceHelper(requireContext())
        val savedName = prefHelper.userName

        // 1. Header Elements
        val btnProfileAvatar = view.findViewById<FrameLayout>(R.id.btnProfileAvatar)
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val tvDepartment = view.findViewById<TextView>(R.id.tvDepartment)
        val btnNotifications = view.findViewById<FrameLayout>(R.id.btnNotifications)
        val btnLogout = view.findViewById<FrameLayout>(R.id.btnLogout)

        // Set initial dynamic greeting based on saved session
        tvGreeting.text = "Welcome, $savedName 👋"

        // 2. Role Switcher
        val btnRoleStudent = view.findViewById<TextView>(R.id.btnRoleStudent)
        val btnRoleFaculty = view.findViewById<TextView>(R.id.btnRoleFaculty)
        val btnRoleAdmin = view.findViewById<TextView>(R.id.btnRoleAdmin)
        val tvRoleBadgeHeader = view.findViewById<TextView>(R.id.tvRoleBadgeHeader)

        // 3. Search Bar
        val etCampusSearch = view.findViewById<EditText>(R.id.etCampusSearch)
        val btnSearchFilter = view.findViewById<ImageView>(R.id.btnSearchFilter)

        // 4. Hero Live Pulse Card & Chips
        val heroCard = view.findViewById<MaterialCardView>(R.id.heroCard)
        val chipCanteenStatus = view.findViewById<TextView>(R.id.chipCanteenStatus)
        val chipLibraryStatus = view.findViewById<TextView>(R.id.chipLibraryStatus)
        val chipNoticeStatus = view.findViewById<TextView>(R.id.chipNoticeStatus)

        // 5. Module Cards (8 Modules)
        val cardNotices = view.findViewById<MaterialCardView>(R.id.cardNotices)
        val cardTimetable = view.findViewById<MaterialCardView>(R.id.cardTimetable)
        val cardCanteen = view.findViewById<MaterialCardView>(R.id.cardCanteen)
        val cardLibrary = view.findViewById<MaterialCardView>(R.id.cardLibrary)
        val cardLostFound = view.findViewById<MaterialCardView>(R.id.cardLostFound)
        val cardHelpdesk = view.findViewById<MaterialCardView>(R.id.cardHelpdesk)
        val cardAlerts = view.findViewById<MaterialCardView>(R.id.cardAlerts)
        val cardAdmin = view.findViewById<MaterialCardView>(R.id.cardAdmin)

        // 6. Urgent Circulars
        val btnViewAllNotices = view.findViewById<TextView>(R.id.btnViewAllNotices)
        val cardCircular1 = view.findViewById<MaterialCardView>(R.id.cardCircular1)
        val cardCircular2 = view.findViewById<MaterialCardView>(R.id.cardCircular2)

        // 7. Bottom Navigation Dock
        val navTabHome = view.findViewById<LinearLayout>(R.id.navTabHome)
        val navTabNotices = view.findViewById<LinearLayout>(R.id.navTabNotices)
        val navTabCanteen = view.findViewById<LinearLayout>(R.id.navTabCanteen)
        val navTabLibrary = view.findViewById<LinearLayout>(R.id.navTabLibrary)
        val navTabProfile = view.findViewById<LinearLayout>(R.id.navTabProfile)

        // ==================== ROLE SWITCHER LOGIC ====================
        fun updateRoleUI(role: UserRole) {
            currentRole = role
            val activeBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_role_active)
            val inactiveBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_role_inactive)
            val activeColor = ContextCompat.getColor(requireContext(), R.color.role_active_text)
            val inactiveColor = ContextCompat.getColor(requireContext(), R.color.role_inactive_text)

            btnRoleStudent.background = if (role == UserRole.STUDENT) activeBg else inactiveBg
            btnRoleStudent.setTextColor(if (role == UserRole.STUDENT) activeColor else inactiveColor)

            btnRoleFaculty.background = if (role == UserRole.FACULTY) activeBg else inactiveBg
            btnRoleFaculty.setTextColor(if (role == UserRole.FACULTY) activeColor else inactiveColor)

            btnRoleAdmin.background = if (role == UserRole.ADMIN) activeBg else inactiveBg
            btnRoleAdmin.setTextColor(if (role == UserRole.ADMIN) activeColor else inactiveColor)

            when (role) {
                UserRole.STUDENT -> {
                    tvGreeting.text = "Welcome, $savedName 👋"
                    tvDepartment.text = "GP Rajkot • IT Dept (Sem 4)"
                    tvRoleBadgeHeader.text = "Student Portal"
                    Toast.makeText(requireContext(), "Switched to Student Portal 🎓", Toast.LENGTH_SHORT).show()
                }
                UserRole.FACULTY -> {
                    tvGreeting.text = "Welcome, Prof. $savedName 👨‍🏫"
                    tvDepartment.text = "IT Dept • Faculty of Engineering"
                    tvRoleBadgeHeader.text = "Faculty Portal"
                    Toast.makeText(requireContext(), "Switched to Faculty Dashboard 👨‍🏫", Toast.LENGTH_SHORT).show()
                }
                UserRole.ADMIN -> {
                    tvGreeting.text = "System Admin 🛡️"
                    tvDepartment.text = "Campus Helper Central Admin"
                    tvRoleBadgeHeader.text = "Admin Portal"
                    Toast.makeText(requireContext(), "Switched to Admin Control 🛡️", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRoleStudent.setOnClickListener { updateRoleUI(UserRole.STUDENT) }
        btnRoleFaculty.setOnClickListener { updateRoleUI(UserRole.FACULTY) }
        btnRoleAdmin.setOnClickListener { updateRoleUI(UserRole.ADMIN) }

        // ==================== SEARCH BAR ====================
        etCampusSearch.setOnEditorActionListener { _, _, _ ->
            val query = etCampusSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                Toast.makeText(requireContext(), "Searching campus for '$query'...", Toast.LENGTH_SHORT).show()
            }
            true
        }

        btnSearchFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Filter by: Exams • Notices • Canteen • Library", Toast.LENGTH_SHORT).show()
        }

        // ==================== HERO LIVE PULSE CARD ====================
        heroCard.setOnClickListener {
            showModuleBottomSheet(
                title = "Timetable: Live Lecture",
                subtitle = "Data Structures & Algorithms • Lab 302",
                tag = "IN PROGRESS",
                iconRes = R.drawable.ic_timetable,
                accentColorRes = R.color.splash_accent_primary,
                description = "You are scheduled for DSA Lab with Prof. Mehta in Lab 302. Attendance verification is open on your device.",
                highlightHeader = "Today's Agenda",
                highlightBody = "• Trees & Binary Search Tree Implementation\n• Lab Assignment 4 Due Date: Today 5:00 PM\n• Next Lecture: Web Tech at 1:30 PM",
                btnText = "View Full Timetable"
            )
        }

        chipCanteenStatus.setOnClickListener { cardCanteen.performClick() }
        chipLibraryStatus.setOnClickListener { cardLibrary.performClick() }
        chipNoticeStatus.setOnClickListener { cardNotices.performClick() }

        // ==================== 8 MODULE CARD CLICK HANDLERS ====================

        // 1. Notices & Circulars
        cardNotices.setOnClickListener {
            showModuleBottomSheet(
                title = "Campus Notices & Circulars",
                subtitle = "Government Polytechnic - Rajkot • IT Dept",
                tag = "3 NEW",
                iconRes = R.drawable.ic_notices,
                accentColorRes = R.color.accent_cyan,
                description = "Centralized official announcements from Gujarat Technological University (GTU) and the IT Department.",
                highlightHeader = "Active Circulars",
                highlightBody = "1. Mid-Semester Exam Schedule 2026 (PDF)\n2. Annual TechFest 'IGNITE 2026' Team Registration\n3. IT Lab 3 Server Upgrades Notice",
                btnText = "📥 Download All Circulars (PDF)"
            )
        }

        // 2. Timetable & Events
        cardTimetable.setOnClickListener {
            showModuleBottomSheet(
                title = "Timetable & Events",
                subtitle = "IT Department • Semester 4 Schedule",
                tag = "TODAY",
                iconRes = R.drawable.ic_timetable,
                accentColorRes = R.color.splash_accent_primary,
                description = "Smart interactive schedules with real-time class cancellations, room swap alerts, and upcoming campus hackathons.",
                highlightHeader = "Today's Schedule",
                highlightBody = "• 11:00 AM - 12:00 PM: Data Structures (Lab 302)\n• 01:30 PM - 02:30 PM: Web Technology (Room 204)\n• 03:00 PM - 04:30 PM: Database Project Review",
                btnText = "📅 Export to Google Calendar"
            )
        }

        // 3. Canteen Hub
        cardCanteen.setOnClickListener {
            showModuleBottomSheet(
                title = "Canteen Hub & Menu",
                subtitle = "Main Campus Food Court • Live Counter",
                tag = "🟢 OPEN",
                iconRes = R.drawable.ic_canteen,
                accentColorRes = R.color.accent_amber,
                description = "Check real-time counter rush, browse today's fresh menu, and pre-book food tokens without standing in queues.",
                highlightHeader = "Today's Fresh Specials",
                highlightBody = "• Paneer Butter Masala Thali — ₹80\n• Samosa & Masala Chai Combo — ₹30\n• Fresh Cold Coffee & Sandwich — ₹50\n• Estimated Wait: 4-6 minutes",
                btnText = "🎟️ Pre-Order Token"
            )
        }

        // 4. Digital Library
        cardLibrary.setOnClickListener {
            showModuleBottomSheet(
                title = "Digital Library & Catalogue",
                subtitle = "Central Library • 5,420+ Books Available",
                tag = "38 SEATS FREE",
                iconRes = R.drawable.ic_library,
                accentColorRes = R.color.accent_emerald,
                description = "Instant book availability search, due date reminders, digital syllabus repository, and quiet study room seat tracker.",
                highlightHeader = "Your Issued Books",
                highlightBody = "• 'Operating System Concepts' (Silberschatz) — Due in 3 days\n• 'Database System Concepts' (Korth) — Renewed\n• Free Seats in Reading Room: 38 / 50",
                btnText = "🔍 Search 5,000+ Books"
            )
        }

        // 5. Lost & Found
        cardLostFound.setOnClickListener {
            showModuleBottomSheet(
                title = "Lost & Found Central Hub",
                subtitle = "Campus-wide Tracking & Claim System",
                tag = "4 ACTIVE",
                iconRes = R.drawable.ic_lost_found,
                accentColorRes = R.color.accent_rose,
                description = "Report misplaced campus items with photo tags or browse recently recovered articles verified by campus security.",
                highlightHeader = "Recently Reported Items",
                highlightBody = "• [FOUND] Boat Airdopes Case (Library 2nd Floor)\n• [FOUND] Fastrack Black Watch (Canteen Area)\n• [LOST] Blue Spiral Notebook (Lab 302)\n• [FOUND] GP Rajkot Student ID Card",
                btnText = "➕ Report Lost / Found Item"
            )
        }

        // 6. Helpdesk & Grievances
        cardHelpdesk.setOnClickListener {
            showModuleBottomSheet(
                title = "Helpdesk & Grievance Portal",
                subtitle = "Student-to-Department Direct Ticketing",
                tag = "24/7 ACTIVE",
                iconRes = R.drawable.ic_helpdesk,
                accentColorRes = R.color.accent_sky,
                description = "Directly report department issues, request lab assistance, or get instant AI responses for recurring campus queries.",
                highlightHeader = "Ticket Status",
                highlightBody = "• Ticket #1042: Lab 3 WiFi Connectivity — [Resolved ✅]\n• Ticket #1089: Projector replacement — [In Progress ⚙️]\n• Average response time: < 24 hours",
                btnText = "📝 Submit Grievance Ticket"
            )
        }

        // 7. Campus Alerts
        cardAlerts.setOnClickListener {
            showModuleBottomSheet(
                title = "Emergency & Broadcast Alerts",
                subtitle = "Real-time Campus Notifications (Firebase FCM)",
                tag = "INSTANT",
                iconRes = R.drawable.ic_bell,
                accentColorRes = R.color.accent_coral,
                description = "High-priority push broadcast system for emergency campus notices, GTU circulars, weather alerts, and fests.",
                highlightHeader = "Recent Broadcasts",
                highlightBody = "• Urgent: Heavy rain alert issued by GTU for tomorrow\n• Reminder: Submit project synopsis before Friday 4 PM\n• Seminar on Cloud Computing at 2 PM Auditorium",
                btnText = "🔔 Manage Notification Settings"
            )
        }

        // 8. Admin & Staff Portal
        cardAdmin.setOnClickListener {
            showModuleBottomSheet(
                title = "Faculty & Admin Console",
                subtitle = "Department Management Control Panel",
                tag = "STAFF ONLY",
                iconRes = R.drawable.ic_admin,
                accentColorRes = R.color.accent_teal,
                description = "Authorized faculty and department administrators can publish circulars, manage daily canteen menus, and resolve grievances.",
                highlightHeader = "Admin Control Actions",
                highlightBody = "• Publish New Notice / Upload PDF\n• Update Canteen Menu & Counter Status\n• Review Student Complaints & Grievances\n• Approve Lost & Found Claim Verifications",
                btnText = "⚙️ Access Admin Dashboard"
            )
        }

        // ==================== URGENT CIRCULAR HIGHLIGHTS ====================
        btnViewAllNotices.setOnClickListener { cardNotices.performClick() }
        cardCircular1.setOnClickListener { cardNotices.performClick() }
        cardCircular2.setOnClickListener { cardTimetable.performClick() }

        // ==================== NOTIFICATIONS & PROFILE ====================
        btnNotifications.setOnClickListener { cardAlerts.performClick() }
        btnProfileAvatar.setOnClickListener {
            showModuleBottomSheet(
                title = prefHelper.userName,
                subtitle = "Email: ${prefHelper.userEmail}",
                tag = "ACTIVE",
                iconRes = R.drawable.ic_nav_profile,
                accentColorRes = R.color.splash_accent_primary,
                description = "Student Profile • Diploma in Information Technology at Government Polytechnic - Rajkot.",
                highlightHeader = "Academic Details",
                highlightBody = "• Role: ${prefHelper.userRole}\n• Department: IT Engineering\n• Session: Persistent Active\n• Registered in: IGNITE 2026 TechFest",
                btnText = "Edit Profile & Settings"
            )
        }

        // ==================== CUSTOM GLASSMORPHIC LOGOUT DIALOG ====================
        btnLogout.setOnClickListener {
            showCustomLogoutDialog()
        }

        // ==================== BOTTOM NAVIGATION DOCK ====================
        fun selectNavTab(tabIndex: Int) {
            activeNavTab = tabIndex
            val activeColor = ContextCompat.getColor(requireContext(), R.color.splash_accent_primary)
            val inactiveColor = ContextCompat.getColor(requireContext(), R.color.nav_item_inactive)

            val icons = listOf(
                view.findViewById<ImageView>(R.id.navIconHome),
                view.findViewById<ImageView>(R.id.navIconNotices),
                view.findViewById<ImageView>(R.id.navIconCanteen),
                view.findViewById<ImageView>(R.id.navIconLibrary),
                view.findViewById<ImageView>(R.id.navIconProfile)
            )

            val texts = listOf(
                view.findViewById<TextView>(R.id.navTextHome),
                view.findViewById<TextView>(R.id.navTextNotices),
                view.findViewById<TextView>(R.id.navTextCanteen),
                view.findViewById<TextView>(R.id.navTextLibrary),
                view.findViewById<TextView>(R.id.navTextProfile)
            )

            for (i in icons.indices) {
                val color = if (i == tabIndex) activeColor else inactiveColor
                icons[i].imageTintList = ColorStateList.valueOf(color)
                texts[i].setTextColor(color)
            }
        }

        navTabHome.setOnClickListener { selectNavTab(0) }
        navTabNotices.setOnClickListener {
            selectNavTab(1)
            cardNotices.performClick()
        }
        navTabCanteen.setOnClickListener {
            selectNavTab(2)
            cardCanteen.performClick()
        }
        navTabLibrary.setOnClickListener {
            selectNavTab(3)
            cardLibrary.performClick()
        }
        navTabProfile.setOnClickListener {
            selectNavTab(4)
            btnProfileAvatar.performClick()
        }
    }

    // ==================== CUSTOM LOGOUT DIALOG ====================
    private fun showCustomLogoutDialog() {
        val logoutDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout_custom, null)

        val tvLogoutUserName = dialogView.findViewById<TextView>(R.id.tvLogoutUserName)
        val tvLogoutUserEmail = dialogView.findViewById<TextView>(R.id.tvLogoutUserEmail)
        val btnCancelLogout = dialogView.findViewById<Button>(R.id.btnCancelLogout)
        val btnConfirmLogout = dialogView.findViewById<Button>(R.id.btnConfirmLogout)

        tvLogoutUserName.text = prefHelper.userName
        tvLogoutUserEmail.text = prefHelper.userEmail

        btnCancelLogout.setOnClickListener {
            logoutDialog.dismiss()
        }

        btnConfirmLogout.setOnClickListener {
            logoutDialog.dismiss()

            // 1. Sign out from Firebase
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                // Handled gracefully
            }

            // 2. Clear local persistent session
            prefHelper.clearSession()

            Toast.makeText(requireContext(), "Logged out successfully. See you soon! 👋", Toast.LENGTH_SHORT).show()

            // 3. Navigate back to Login screen
            findNavController().navigate(R.id.action_homeFragment_to_FirstFragment)
        }

        logoutDialog.setContentView(dialogView)
        logoutDialog.show()
    }

    // ==================== INTERACTIVE BOTTOM SHEET PREVIEW ====================
    private fun showModuleBottomSheet(
        title: String,
        subtitle: String,
        tag: String,
        iconRes: Int,
        accentColorRes: Int,
        description: String,
        highlightHeader: String,
        highlightBody: String,
        btnText: String
    ) {
        val bottomSheet = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.layout_bottom_sheet_module, null)

        val sheetIcon = sheetView.findViewById<ImageView>(R.id.sheetIcon)
        val sheetTitle = sheetView.findViewById<TextView>(R.id.sheetTitle)
        val sheetSubtitle = sheetView.findViewById<TextView>(R.id.sheetSubtitle)
        val sheetTag = sheetView.findViewById<TextView>(R.id.sheetTag)
        val sheetBtnClose = sheetView.findViewById<ImageView>(R.id.sheetBtnClose)
        val sheetDescription = sheetView.findViewById<TextView>(R.id.sheetDescription)
        val sheetHighlightHeader = sheetView.findViewById<TextView>(R.id.sheetHighlightHeader)
        val sheetHighlightBody = sheetView.findViewById<TextView>(R.id.sheetHighlightBody)
        val sheetPrimaryBtn = sheetView.findViewById<MaterialButton>(R.id.sheetPrimaryBtn)

        val accentColor = ContextCompat.getColor(requireContext(), accentColorRes)
        sheetIcon.setImageResource(iconRes)
        sheetIcon.imageTintList = ColorStateList.valueOf(accentColor)
        sheetTag.setTextColor(accentColor)
        sheetPrimaryBtn.backgroundTintList = ColorStateList.valueOf(accentColor)

        sheetTitle.text = title
        sheetSubtitle.text = subtitle
        sheetTag.text = tag
        sheetDescription.text = description
        sheetHighlightHeader.text = highlightHeader
        sheetHighlightBody.text = highlightBody
        sheetPrimaryBtn.text = btnText

        sheetBtnClose.setOnClickListener { bottomSheet.dismiss() }
        sheetPrimaryBtn.setOnClickListener {
            Toast.makeText(requireContext(), "$title action triggered! 🚀", Toast.LENGTH_SHORT).show()
            bottomSheet.dismiss()
        }

        bottomSheet.setContentView(sheetView)
        bottomSheet.show()
    }
}