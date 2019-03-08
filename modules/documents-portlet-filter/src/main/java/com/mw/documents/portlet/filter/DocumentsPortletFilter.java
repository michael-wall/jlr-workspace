package com.mw.documents.portlet.filter;

import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.PortletFilter;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.RenderResponseWrapper;

import org.osgi.service.component.annotations.Component;

/**
 * Adds the Authenticator Code field to the Login Portlet login form below the Password field if tfaConfiguration.loginTotp2faEnabled true.
 *
 * @author Michael Wall
 */
@Component(
	    immediate = true,
	    property = {
	         "javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY,
	         "javax.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY_ADMIN
	    },
	    service = PortletFilter.class)
public class DocumentsPortletFilter implements RenderFilter{
	

	
	@Override
	public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain)
			throws IOException, PortletException {

		RenderResponseWrapper renderResponseWrapper = new BufferedRenderResponseWrapper(response);
		
		chain.doFilter(request, renderResponseWrapper);
		
		String text = renderResponseWrapper.toString();
		String textCleansed = text.replaceAll("\\n", "");
		textCleansed = textCleansed.replaceAll("\\r", "");

		//The JSP is used by both DLPortlet and DLAdminPortlet meaning there is 2 form tag variations...
		
		boolean formMatches = textCleansed.matches(".*(<form action).*(id=\"_com_liferay_document_library_web_portlet_DL(Admin)?Portlet_fm2\" method=\"post\" name=\"_com_liferay_document_library_web_portlet_DL(Admin)?Portlet_fm2\"\\s>).*(</form>).*");

		if (_log.isDebugEnabled()) {
			_log.debug("formMatches: " + formMatches);	
		}

		boolean formButtonMatches = textCleansed.matches(".*(<form action).*(id=\"_com_liferay_document_library_web_portlet_DL(Admin)?Portlet_fm2\" method=\"post\" name=\"_com_liferay_document_library_web_portlet_DL(Admin)?Portlet_fm2\"\\s>).*(<button).*(id=\"_com_liferay_document_library_web_portlet_DL(Admin)?Portlet_)[a-z]{4}(\").*(type=\"submit\").*(>).*(</button>).*(</form>).*");

		if (_log.isDebugEnabled()) {
			_log.debug("formButtonMatches: " + formButtonMatches);	
		}

		if (formMatches && formButtonMatches) {
			Pattern buttonHtmlPattern = Pattern.compile("(<button).*(id=\"_com_liferay_document_library_web_portlet_DL(Admin)?Portlet_)[a-z]{4}(\").*(type=\"submit\").*(>).*(</button>)", Pattern.DOTALL);
		    Matcher buttonHtmlMatcher = buttonHtmlPattern.matcher(text);
		    boolean buttonHtmlMatchFound = buttonHtmlMatcher.find();
		    
		    if (_log.isDebugEnabled()) {
		    	 _log.info("buttonHtmlMatchFound: " + buttonHtmlMatchFound);
		    }

		    if (buttonHtmlMatchFound) {
		    	int buttonHtmlEndIndex = buttonHtmlMatcher.end();
		    
		    	//Up to the end of the button close tag.
				String pre = text.substring(0, buttonHtmlEndIndex);

				//After button close tag.
				String post = text.substring(buttonHtmlEndIndex + 1);
				
				//TODO MW Externalize
				text = pre + "&nbsp;<span><strong>Please wait until all the Files are uploaded before clicking.</strong></span>" + post;
		    }
		}

		response.getWriter().write(text);
	}

	@Override
	public void init(FilterConfig filterConfig) throws PortletException {

	}

	@Override
	public void destroy() {

	}	

	private static Log _log = LogFactoryUtil.getLog(DocumentsPortletFilter.class);
}