# CSS Organization Guide

## 📁 File Structure

All CSS files are now organized in a dedicated folder:

```
src/main/resources/static/css/
├── landing.css          # Landing page styles
├── login.css            # Login page styles  
├── register.css         # Registration page styles
├── dashboard.css        # Shared dashboard styles (Student & Teacher)
└── verification.css     # Email verification pages styles
```

## 📄 CSS to HTML Mapping

| HTML Template | CSS File | Purpose |
|--------------|----------|---------|
| `landing.html` | `landing.css` | Homepage with features and support section |
| `login.html` | `login.css` | User login form |
| `register.html` | `register.css` | User registration form with role selection |
| `student-dashboard.html` | `dashboard.css` | Student dashboard view |
| `teacher-dashboard.html` | `dashboard.css` | Teacher dashboard view (shares same styles) |
| `resend-verification.html` | `verification.css` | Resend verification email form |
| `verify-result.html` | `verification.css` | Email verification result page |

## 🎨 CSS Files Details

### 1. **landing.css** (Landing Page)
- **Purpose**: Homepage styling
- **Key Classes**:
  - `.header` - Top navigation bar
  - `.hero` - Hero section with gradient background
  - `.feature-card` - Feature showcase cards
  - `.support-section` - Support options section
  - `.chat-widget` - Fixed chat button

- **Color Variables**:
  ```css
  --eac-red: #cc0000
  --eac-gold: #FFD700
  ```

### 2. **login.css** (Login Page)
- **Purpose**: Login form styling
- **Key Classes**:
  - `.login-container` - Main form container
  - `.logo-section` - EAC logo and title
  - `.alert-error`, `.alert-success`, `.alert-info` - Alert messages
  - `.form-group` - Form field containers
  - `.btn` - Primary button
  - `.back-link` - Navigation link

### 3. **register.css** (Registration Page)
- **Purpose**: Registration form styling
- **Key Classes**:
  - `.register-container` - Main form container (scrollable)
  - `.logo-section` - EAC logo and title
  - `.alert-error` - Error messages
  - `.form-group` - Form field containers
  - `.required` - Required field indicator
  - `.help-text` - Field help text
  - `#studentIdGroup` - Conditional student ID field

- **JavaScript Interaction**:
  - Shows/hides student ID field based on role selection

### 4. **dashboard.css** (Dashboards - Shared)
- **Purpose**: Shared styling for Student and Teacher dashboards
- **Key Classes**:
  - `.header` - Dashboard top bar
  - `.nav-menu` - Navigation menu
  - `.btn-logout` - Logout button
  - `.welcome-card` - Welcome message card
  - `.dashboard-grid` - Responsive card grid
  - `.card` - Dashboard action cards
  - `.card-icon` - Material icon in cards

- **Responsive**: Auto-fit grid with 300px minimum

### 5. **verification.css** (Verification Pages - Shared)
- **Purpose**: Styling for email verification pages
- **Key Classes**:
  - `.container` - Centered container
  - `.success-icon`, `.error-icon` - Status icons
  - `.alert-success`, `.alert-error` - Alert messages
  - `.form-group` - Form fields
  - `.btn` - Action button
  - `.back-link` - Navigation link

## 🎯 Design Consistency

### Colors
All files use consistent EAC branding:
- **Primary**: `#cc0000` (EAC Red)
- **Dark**: `#990000` (Darker Red for hover states)
- **Gold Accent**: `#FFD700` (EAC Gold)

### Typography
- **Font Family**: 'Google Sans', Arial, sans-serif
- **Headings**: Bold (500-700 weight)
- **Body**: Regular (400 weight)

### Spacing
- Container padding: `2rem` to `2.5rem`
- Card margins: `1.5rem` to `2rem`
- Form fields: `1.5rem` bottom margin

### Effects
- **Transitions**: 0.3s for all interactive elements
- **Hover Effects**: `translateY(-2px)` or `scale(1.05)`
- **Shadows**: Box-shadow on hover for cards and buttons
- **Border Radius**: 6px to 12px for modern look

## 🔧 Customization Guide

### To Change Primary Color:
Update `:root` variables in each CSS file:
```css
:root {
    --eac-red: #YOUR_COLOR;
}
```

### To Add New Styles:
1. Identify which page needs styling
2. Open corresponding CSS file
3. Add new classes following naming convention
4. Use existing variables for consistency

### To Create New Page:
1. Create new CSS file in `/static/css/`
2. Use existing CSS as template
3. Include EAC color variables
4. Link in HTML: `<link rel="stylesheet" th:href="@{/css/your-file.css}">`

## ✅ Benefits of Separation

### 1. **Maintainability**
- Easy to find and update styles
- No need to search through HTML
- Clear file organization

### 2. **Reusability**
- `dashboard.css` shared by student and teacher dashboards
- `verification.css` shared by verification pages
- Common styles can be extracted to shared files

### 3. **Performance**
- CSS files are cached by browser
- Reduces HTML file size
- Faster page loads on subsequent visits

### 4. **Collaboration**
- Frontend developers can work on CSS independently
- Backend developers focus on HTML logic
- Reduced merge conflicts

### 5. **Debugging**
- Browser DevTools shows separate CSS files
- Easy to identify which file contains specific styles
- Clear source mapping

## 📝 Naming Conventions

### Files:
- Lowercase with hyphens: `student-dashboard.css`
- Descriptive names matching HTML templates

### Classes:
- Kebab-case: `.login-container`, `.feature-card`
- BEM-style for components: `.card`, `.card-icon`, `.card-title`
- State classes: `.alert-error`, `.alert-success`

### Variables:
- Double-dash prefix: `--eac-red`, `--eac-gold`
- Descriptive names: `--primary-color`, `--dark-red`

## 🚀 Next Steps

If you need to add more pages:

1. **Chat Pages** → `css/chat.css`
2. **Classroom Pages** → `css/classroom.css`
3. **Assignment Pages** → `css/assignments.css`
4. **Profile Pages** → `css/profile.css`

Remember to:
- Include EAC color scheme
- Use Google Sans font
- Follow existing patterns
- Test responsive design

---

**All CSS files are production-ready and organized for scalability!** 🎨
